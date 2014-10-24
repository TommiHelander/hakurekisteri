package fi.vm.sade.hakurekisteri.integration.virta

import java.io.InterruptedIOException
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.event.Logging
import com.stackmob.newman.{ApacheHttpClient, HttpClient}
import com.stackmob.newman.dsl._
import com.stackmob.newman.response.HttpResponseCode
import fi.vm.sade.generic.common.HetuUtils
import org.joda.time.format.DateTimeFormat

import scala.compat.Platform
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, Node, NodeSeq, XML}
import java.util.concurrent.{ThreadFactory, Executors}
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.HttpConnectionParams
import org.apache.http.impl.NoConnectionReuseStrategy

case class VirtaValidationError(m: String) extends Exception(m)

object VirtaClient {

  val threadNumber = new AtomicInteger(1)
  val pool = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(50, new ThreadFactory() {
    override def newThread(r: Runnable): Thread = {
      new Thread(r, "virta-request" + "-" + threadNumber.getAndIncrement)
    }
  }))
  private def createApacheHttpClient(maxConnections: Int): org.apache.http.client.HttpClient = {
    val connManager: ClientConnectionManager = {
      val cm = new PoolingClientConnectionManager()
      cm.setDefaultMaxPerRoute(maxConnections)
      cm.setMaxTotal(maxConnections)
      cm
    }

    val client = new DefaultHttpClient(connManager)
    val httpParams = client.getParams
    HttpConnectionParams.setConnectionTimeout(httpParams, 10000)
    HttpConnectionParams.setSoTimeout(httpParams, 120000)
    HttpConnectionParams.setStaleCheckingEnabled(httpParams, false)
    HttpConnectionParams.setSoKeepalive(httpParams, false)
    client.setReuseStrategy(new NoConnectionReuseStrategy())
    client
  }

  val defaultClient: HttpClient =
    new ApacheHttpClient(createApacheHttpClient(100))(pool)

}

class VirtaClient(config: VirtaConfig = VirtaConfig(serviceUrl = "http://virtawstesti.csc.fi/luku/OpiskelijanTiedot",
                                                    jarjestelma = "",
                                                    tunnus = "",
                                                    avain = "salaisuus"), client: HttpClient = VirtaClient.defaultClient)
                 (implicit val system: ActorSystem) {


  implicit val httpClient = client
  implicit val ec = VirtaClient.pool

  val logger = Logging.getLogger(system, this)
  val maxRetries = 3

  def getOpiskelijanTiedot(oppijanumero: String, hetu: Option[String] = None): Future[Option[VirtaResult]] = {
    if (hetu.isDefined && !HetuUtils.isHetuValid(hetu.get)) throw new IllegalArgumentException("hetu is not valid")

    val operation =
<OpiskelijanKaikkiTiedotRequest xmlns="http://tietovaranto.csc.fi/luku">
  <Kutsuja>
    <jarjestelma>{config.jarjestelma}</jarjestelma>
    <tunnus>{config.tunnus}</tunnus>
    <avain>{config.avain}</avain>
  </Kutsuja>
  <Hakuehdot>
    {if (hetu.isDefined) <henkilotunnus>{hetu.get}</henkilotunnus> else <kansallinenOppijanumero>{oppijanumero}</kansallinenOppijanumero>}
  </Hakuehdot>
</OpiskelijanKaikkiTiedotRequest>

    val retryCount = new AtomicInteger(1)
    tryPost(config.serviceUrl, wrapSoapEnvelope(operation), oppijanumero, hetu, retryCount)
  }

  def parseFault(response: String): Unit = {
    Try(XML.loadString(response)) match {
      case Success(xml) =>
        val fault: NodeSeq = xml \ "Body" \ "Fault"
        if (fault.nonEmpty) {
          val faultstring = fault \ "faultstring"
          val faultdetail = fault \ "detail" \ "ValidationError"
          if (faultstring.text.toLowerCase == "validation error") {
            logger.warning(s"validation error: ${faultdetail.text}")
            throw VirtaValidationError(s"validation error: ${faultdetail.text}")
          }
        }
      case _ =>
    }
  }

  def tryPost(url: String, requestEnvelope: String, oppijanumero: String, hetu: Option[String], retryCount: AtomicInteger): Future[Option[VirtaResult]] = {
    val start = Platform.currentTime
    POST(new URL(url)).setBodyString(requestEnvelope).apply.map((response) => {
      logger.info(s"virta query for $oppijanumero took ${Platform.currentTime - start} ms, response code ${response.code}")
      if (response.code == HttpResponseCode.Ok) {
        val responseEnvelope: Elem = XML.loadString(response.bodyString)

        val opiskeluoikeudet = getOpiskeluoikeudet(responseEnvelope)
        val tutkinnot = getTutkinnot(responseEnvelope)

        (opiskeluoikeudet, tutkinnot) match {
          case (Seq(), Seq()) =>
            None

          case _ =>
            Some(VirtaResult(opiskeluoikeudet = opiskeluoikeudet, tutkinnot = tutkinnot))

        }
      } else {
        val bodyString = response.bodyString

        parseFault(bodyString)

        logger.error(s"got non-ok response from virta: ${response.code}, response: $bodyString")
        throw VirtaConnectionErrorException(s"got non-ok response from virta: ${response.code}, response: $bodyString")
      }
    }).recoverWith {
      case t: InterruptedIOException =>
        if (retryCount.getAndIncrement <= maxRetries) {
          logger.warning(s"got $t, retrying virta query for $oppijanumero: retryCount ${retryCount.get}")

          tryPost(url, requestEnvelope, oppijanumero, hetu, retryCount)
        } else Future.failed(t)
    }
  }

  def getOpiskeluoikeudet(response: NodeSeq): Seq[VirtaOpiskeluoikeus] = {
    val opiskeluoikeudet: NodeSeq = response \ "Body" \ "OpiskelijanKaikkiTiedotResponse" \ "Virta" \ "Opiskelija" \ "Opiskeluoikeudet" \ "Opiskeluoikeus"
    opiskeluoikeudet.map((oo: Node) => {
      val avain = oo.map(_ \ "@avain")

      VirtaOpiskeluoikeus(
        alkuPvm = Try(DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate((oo \ "AlkuPvm").head.text)).get,
        loppuPvm = Try(DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate((oo \ "LoppuPvm").head.text)).toOption,
        myontaja = extractTextOption(oo \ "Myontaja" \ "Koodi", avain, required = true).get,
        koulutuskoodit = Try((oo \ "Jakso" \ "Koulutuskoodi").map(_.text)).get,
        opintoala1995 = extractTextOption(oo \ "Opintoala1995", avain), // Universities use this
        koulutusala2002 = extractTextOption(oo \ "Koulutusala2002", avain), // AMK
        kieli = resolveKieli(oo \ "Jakso" \ "Kieli")
      )
    })
  }

  def getTutkinnot(response: NodeSeq): Seq[VirtaTutkinto] = {
    val opintosuoritukset: NodeSeq = response \ "Body" \ "OpiskelijanKaikkiTiedotResponse" \ "Virta" \ "Opiskelija" \ "Opintosuoritukset" \ "Opintosuoritus"
    opintosuoritukset.withFilter(filterTutkinto).map((os: Node) => {
      val avain = os.map(_ \ "@avain")

      VirtaTutkinto(
        suoritusPvm = Try(DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate((os \ "SuoritusPvm").head.text)).get,
        koulutuskoodi = extractTextOption(os \ "Koulutuskoodi", avain, required = true),
        opintoala1995 = extractTextOption(os \ "Opintoala1995", avain), // Universities use this
        koulutusala2002 = extractTextOption(os \ "Koulutusala2002", avain), // AMK
        myontaja = extractTextOption(os \ "Myontaja" \ "Koodi", avain, required = true).get,
        kieli = resolveKieli(os \ "Kieli")
      )
    })
  }

  def extractTextOption(n: NodeSeq, avain: Seq[NodeSeq], required: Boolean = false): Option[String] = {
    Try(Some(n.head.text)).orElse{ if (required) Failure(InvalidVirtaResponseException(s"element $n is missing from avain $avain")) else Success(None) }.get
  }

  def resolveKieli(n: NodeSeq): String = {
    Try(n.head.text.toUpperCase).getOrElse("FI") match {
      case "20" => "99"
      case k => k
    }
  }

  def filterTutkinto(n: Node): Boolean = {
    (n \ "Laji").head.text == "1"
  }

  def wrapSoapEnvelope(operation: Elem): String = {
    val buf = new StringBuilder
    buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
    buf.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n")
    buf.append("<SOAP-ENV:Body>\n")
    buf.append(operation.toString())
    buf.append("\n</SOAP-ENV:Body>\n")
    buf.append("</SOAP-ENV:Envelope>\n")
    buf.toString()
  }
}
