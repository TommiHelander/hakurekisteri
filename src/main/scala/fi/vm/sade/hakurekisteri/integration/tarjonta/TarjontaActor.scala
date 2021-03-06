package fi.vm.sade.hakurekisteri.integration.tarjonta

import akka.actor.{Actor, ActorLogging}
import fi.vm.sade.hakurekisteri.{Config, Oids}
import fi.vm.sade.hakurekisteri.integration.{FutureCache, VirkailijaRestClient}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.pattern.pipe
import fi.vm.sade.hakurekisteri.tools.RicherString._
import org.joda.time.LocalDate

case class SearchKomoQuery(koulutus: String)

case class GetKomoQuery(oid: String)

object GetHautQuery

case class RestHakuResult(result: List[RestHaku])

case class GetHautQueryFailedException(m: String, cause: Throwable) extends Exception(m, cause)

case class RestHaku(oid:Option[String],
                    hakuaikas: List[RestHakuAika],
                    nimi: Map[String, String],
                    hakukausiUri: String,
                    hakukausiVuosi: Int,
                    koulutuksenAlkamiskausiUri: Option[String],
                    koulutuksenAlkamisVuosi: Option[Int],
                    kohdejoukkoUri: Option[String],
                    kohdejoukonTarkenne: Option[String],
                    tila: String) {
  def isJatkotutkintohaku = kohdejoukonTarkenne.exists(_.startsWith("haunkohdejoukontarkenne_3#"))
}

case class RestHakuAika(alkuPvm: Long, loppuPvm: Option[Long])

case class TarjontaResultResponse[T](result: T)

case class KomoResponse(oid: String,
                        komo: Option[Komo])

case class TarjontaKoodi(arvo: Option[String])

case class Koulutus(oid: String,
                    komoOid: String,
                    tunniste: Option[String],
                    kandidaatinKoulutuskoodi: Option[TarjontaKoodi],
                    koulutuksenAlkamiskausi: Option[TarjontaKoodi],
                    koulutuksenAlkamisvuosi: Option[Int],
                    koulutuksenAlkamisPvms: Option[Set[Long]])

case class HakukohdeOid(oid: String)

case class Hakukohde(oid: String,
                     hakukohdeKoulutusOids: Seq[String],
                     ulkoinenTunniste: Option[String])

case class Hakukohteenkoulutus(komoOid: String,
                               tkKoulutuskoodi: String,
                               kkKoulutusId: Option[String],
                               koulutuksenAlkamiskausi: Option[TarjontaKoodi],
                               koulutuksenAlkamisvuosi: Option[Int],
                               koulutuksenAlkamisPvms: Option[Set[Long]])

case class HakukohteenKoulutukset(hakukohdeOid: String,
                                  ulkoinenTunniste: Option[String],
                                  koulutukset: Seq[Hakukohteenkoulutus])

class TarjontaException(val m: String) extends Exception(m)
case class HakukohdeNotFoundException(message: String) extends TarjontaException(message)
case class KoulutusNotFoundException(message: String) extends TarjontaException(message)
case class KomoNotFoundException(message: String) extends TarjontaException(message)

class TarjontaActor(restClient: VirkailijaRestClient, config: Config) extends Actor with ActorLogging {
  private val koulutusCache = new FutureCache[String, HakukohteenKoulutukset](config.integrations.tarjontaCacheHours.hours.toMillis)
  private val komoCache = new FutureCache[String, KomoResponse](config.integrations.tarjontaCacheHours.hours.toMillis)
  val maxRetries = config.integrations.tarjontaConfig.httpClientMaxRetries
  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case q: SearchKomoQuery => searchKomo(q.koulutus) pipeTo sender
    case q: GetKomoQuery => getKomo(q.oid) pipeTo sender
    case GetHautQuery => getHaut pipeTo sender
    case oid: HakukohdeOid => getHakukohteenKoulutukset(oid) pipeTo sender
  }
  
  def searchKomo(koulutus: String): Future[Seq[Komo]] = {
    restClient.readObject[TarjontaResultResponse[Seq[Komo]]]("tarjonta-service.komo.search", koulutus)(200, maxRetries).map(_.result)
  }

  def getKomo(oid: String): Future[KomoResponse] = {
    if (komoCache.contains(oid)) komoCache.get(oid)
    else {
      val f = restClient.readObject[TarjontaResultResponse[Option[Komo]]]("tarjonta-service.komo", oid)(200, maxRetries).map(res => KomoResponse(oid, res.result))
      komoCache + (oid, f)
      f
    }
  }

  def includeHaku(haku: RestHaku): Boolean = {
    haku.tila == "JULKAISTU" || (haku.tila == "VALMIS" && haku.isJatkotutkintohaku)
  }

  def getHaut: Future[RestHakuResult] = restClient.readObject[RestHakuResult]("tarjonta-service.haku.findAll")(200).map(res => res.copy(res.result.filter(includeHaku))).recover {
    case t: Throwable =>
      log.error(t, "error retrieving all hakus")
      throw GetHautQueryFailedException("error retrieving all hakus", t)
  }

  def getKoulutus(oid: String): Future[Seq[Hakukohteenkoulutus]] = {
    val koulutus: Future[Option[Koulutus]] = restClient.readObject[TarjontaResultResponse[Option[Koulutus]]]("tarjonta-service.koulutus", oid)(200, maxRetries).map(r => r.result)
    koulutus.flatMap {
      case None => Future.failed(KoulutusNotFoundException(s"koulutus not found with oid $oid"))
      case Some(k) =>
        val fk: Future[Option[Komo]] = getKomo(k.komoOid).map(r => r.komo)
        fk.map {
          case None => throw KomoNotFoundException(s"komo not found with oid ${k.komoOid}")
          case Some(komo) =>
            val kkKoulutusId = k.tunniste.flatMap(_.blankOption)
            val koulutukset = Seq(Hakukohteenkoulutus(komo.oid, komo.koulutuskoodi.arvo, kkKoulutusId, k.koulutuksenAlkamiskausi, k.koulutuksenAlkamisvuosi, k.koulutuksenAlkamisPvms))
            k.kandidaatinKoulutuskoodi.flatMap(_.arvo.map(a => koulutukset :+ Hakukohteenkoulutus(komo.oid, a, kkKoulutusId, k.koulutuksenAlkamiskausi, k.koulutuksenAlkamisvuosi, k.koulutuksenAlkamisPvms))).getOrElse(koulutukset)
        }
    }
  }
  def getHakukohteenkoulutukset(oids: Seq[String]): Future[Seq[Hakukohteenkoulutus]] = Future.sequence(oids.map(getKoulutus)).map(_.foldLeft(Seq[Hakukohteenkoulutus]())(_ ++ _))

  def getHakukohteenKoulutukset(hk: HakukohdeOid): Future[HakukohteenKoulutukset] = {
    if (koulutusCache.contains(hk.oid)) koulutusCache.get(hk.oid)
    else {
      val fh: Future[Option[Hakukohde]] = restClient.readObject[TarjontaResultResponse[Option[Hakukohde]]]("tarjonta-service.hakukohde", hk.oid)(200, maxRetries).map(r => r.result)
      val hks: Future[HakukohteenKoulutukset] = fh.flatMap {
        case None => Future.failed(HakukohdeNotFoundException(s"hakukohde not found with oid ${hk.oid}"))
        case Some(h) => for (
          hakukohteenkoulutukset: Seq[Hakukohteenkoulutus] <- getHakukohteenkoulutukset(h.hakukohdeKoulutusOids)
        ) yield HakukohteenKoulutukset(h.oid, h.ulkoinenTunniste, hakukohteenkoulutukset)
      }
      koulutusCache + (hk.oid, hks)
      hks
    }
  }
}

class MockTarjontaActor(config: Config) extends TarjontaActor(null, config) {

  override def receive: Receive = {
    case GetKomoQuery(oid) =>
      val response: KomoResponse = if (oid == Oids.yotutkintoKomoOid) KomoResponse(oid, Some(Komo(oid, Koulutuskoodi("301101"), "TUTKINTO", "LUKIOKOULUTUS"))) else KomoResponse(oid, None)
      sender ! response

    case GetHautQuery =>
      sender ! RestHakuResult(List(RestHaku(
        oid = Some("1.2.3.4"),
        hakuaikas = List(RestHakuAika(1, Some(new LocalDate().plusMonths(1).toDate.getTime))),
        nimi = Map("kieli_fi" -> "haku 1", "kieli_sv" -> "haku 1", "kieli_en" -> "haku 1"),
        hakukausiUri = "kausi_k#1",
        hakukausiVuosi = new LocalDate().getYear,
        koulutuksenAlkamiskausiUri = Some("kausi_s#1"),
        koulutuksenAlkamisVuosi = Some(new LocalDate().getYear),
        kohdejoukkoUri = Some("haunkohdejoukko_12#1"),
        None,
        tila = "JULKAISTU"
      )))

    case msg =>
      log.warning(s"not implemented receive(${msg})")
  }
}

