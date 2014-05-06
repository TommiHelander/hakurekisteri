package fi.vm.sade.hakurekisteri.hakija

import akka.actor.{Props, ActorRef, Actor}
import scala.concurrent.{Future, ExecutionContext}
import akka.event.Logging
import fi.vm.sade.hakurekisteri.suoritus.Suoritus
import fi.vm.sade.hakurekisteri.henkilo._
import fi.vm.sade.hakurekisteri.opiskelija.Opiskelija
import scala.util.{Failure, Success, Try}
import scala.Some
import fi.vm.sade.hakurekisteri.suoritus.Komoto
import fi.vm.sade.hakurekisteri.henkilo.Yhteystiedot
import akka.pattern.ask
import ForkedSeq._
import TupledFuture._
import fi.vm.sade.hakurekisteri.hakija.HakijaResource.EOF


case class Hakukohde(koulutukset: Set[Komoto], hakukohdekoodi: String)

case class Hakutoive(hakukohde: Hakukohde, kaksoistutkinto: Boolean)

case class Hakemus(hakutoiveet: Seq[Hakutoive], hakemusnumero: String)

case class Hakija(henkilo: Henkilo, suoritukset: Seq[Suoritus], opiskeluhistoria: Seq[Opiskelija], hakemus: Hakemus)

class HakijaActor(hakupalvelu: Hakupalvelu, organisaatioActor: ActorRef, koodistopalvelu: Koodistopalvelu) extends Actor {
  implicit val executionContext: ExecutionContext = context.dispatcher
  val log = Logging(context.system, this)


  def receive = {
    case q: HakijaQuery => XMLQuery(q)
  }



  def resolveOppilaitosKoodi(o:Organisaatio): Future[Option[String]] =  o.oppilaitosKoodi match {
    case None => findOppilaitoskoodi(o.parentOid)
    case Some(k) => Future(Some(k))
  }


  def getOrg(oid: String): Future[Option[Organisaatio]] = {
    import scala.concurrent.duration._
    implicit val timeout: akka.util.Timeout = 30.seconds
    Try((organisaatioActor ? oid).mapTo[Option[Organisaatio]]).getOrElse(Future.successful(None))
  }

  def findOppilaitoskoodi(parentOid: Option[String]): Future[Option[String]] = parentOid match {
    case None => Future(None)
    case Some(oid) => getOrg(oid).flatMap(_.map(resolveOppilaitosKoodi).getOrElse(Future(None)))

  }

  def hakutoive2XMLHakutoive(ht: Hakutoive, jno:Int): Future[Option[XMLHakutoive]] =  {
   for(
      orgData: Option[(Organisaatio, String)] <- findOrgData(ht.hakukohde.koulutukset.head.tarjoaja)
    ) yield
     for ((org: Organisaatio, oppilaitos: String) <- orgData)
      yield XMLHakutoive(ht,jno,org,oppilaitos)
  }

  @Deprecated // TODO mäppää puuttuvat tiedot
  def getXmlHakutoiveet(hakija: Hakija): Future[Seq[XMLHakutoive]] = {
    val futureToiveet = for ((ht, jno) <- hakija.hakemus.hakutoiveet.zipWithIndex)  yield hakutoive2XMLHakutoive(ht, jno)
    futureToiveet.join.map(_.flatten)
  }

  def extractOption(t: (Option[Organisaatio], Option[String])): Option[(Organisaatio, String)] = t._1 match {
    case None => None
    case Some(o) => Some((o, t._2.get))
  }

  def findOrgData(tarjoaja: String): Future[Option[(Organisaatio,String)]] = {
    getOrg(tarjoaja).flatMap((o) => findOppilaitoskoodi(o.map(_.oid)).map(k => extractOption(o, k)))
  }


  def createHakemus(hakija: Hakija)(opiskelija: Option[Opiskelija], org:Option[Organisaatio], ht: Seq[XMLHakutoive]) = XMLHakemus(hakija,opiskelija, org, ht)


  @Deprecated // TODO mäppää puuttuvat tiedot
  def getXmlHakemus(hakija: Hakija): Future[XMLHakemus] = {

    val (opiskelutieto, lahtokoulu) = getOpiskelijaTiedot(hakija)
    val ht: Future[Seq[XMLHakutoive]] = getXmlHakutoiveet(hakija)
    val data = (opiskelutieto,lahtokoulu,ht).join

    data.tupledMap(createHakemus(hakija))
  }

  def getOpiskelijaTiedot(hakija: Hakija): (Future[Option[Opiskelija]], Future[Option[Organisaatio]]) = hakija.opiskeluhistoria match {
    case opiskelijaTiedot :: _ => (Future.successful(Some(opiskelijaTiedot)), getOrg(opiskelijaTiedot.oppilaitosOid))
    case _ => (Future.successful(None),Future.successful(None))
  }


  def getMaakoodi(koodiArvo: String): Future[String] = koodiArvo.toLowerCase match {
    case "fin" => Future.successful("246")
    case arvo => koodistopalvelu.getRinnasteinenKoodiArvo("maatjavaltiot1_" + arvo, "maatjavaltiot2")
  }


  def hakija2XMLHakija(hakija:Hakija): Future[XMLHakija] = {
    enrich(hakija).tupledMap(data2XmlHakija(hakija))
  }

  def enrich(hakija: Hakija) = {
    val hakemus: Future[XMLHakemus] = getXmlHakemus(hakija)
    val yhteystiedot: Seq[Yhteystiedot] = hakija.henkilo.yhteystiedotRyhma.getOrElse(("hakemus", "yhteystietotyyppi1"), Seq())
    val maakoodi = Try(getMaakoodi(yhteystiedot.getOrElse("YHTEYSTIETO_MAA", "FIN"))).transform(s => Success(s), t => {log.error("%s failed to fetch country".format(hakija));Failure(t)}).get
    val kansalaisuus = Try(getMaakoodi(Try(hakija.henkilo.kansalaisuus.head).map(k => k.kansalaisuusKoodi).getOrElse("FIN"))).transform(s => Success(s), t => {log.error("%s failed to fetch country".format(hakija));Failure(t)}).get

    (hakemus, Future.successful(yhteystiedot), maakoodi, kansalaisuus).join
  }

  def data2XmlHakija(hakija:Hakija)(hakemus:XMLHakemus, yhteystiedot: Seq[Yhteystiedot], kotimaa:String, kansalaisuus:String) =
    XMLHakija(hakija, yhteystiedot, kotimaa, kansalaisuus, hakemus)

  def hakijat2XmlHakijat(hakijat:Seq[Hakija]) = hakijat.map(hakija2XMLHakija).join.map(XMLHakijat)


  def XMLQuery(q: HakijaQuery): Unit = q.hakuehto match {
    case Hakuehto.Kaikki => val streamer = sender
                            context.actorOf(Props(new Haku(q,streamer)))
    // TODO Hakuehto.Hyväksytyt & Hakuehto.Vastaanottaneet
    case _ => sender ! EOF
  }

  class Haku(q: HakijaQuery, streamer: ActorRef) extends Actor {

    var page = 0
    var fetching = true
    var found = 0
    var sent = 0

    import akka.pattern._

    override def preStart(): Unit = nextPage


    def nextPage {
      log.info("%s getting page %d of query %s" format (self, page,q))
      hakupalvelu.getHakijat(q, page) pipeTo self
    }

    override def receive: Actor.Receive = {
      case hakijat:Seq[Hakija] => log.info("found %d applicants" format hakijat.length)
                                  found = found + hakijat.length
                                  if (hakijat.length == hakupalvelu.maxApplications) {
                                    page = page + 1
                                    nextPage
                                  } else fetching = false
                                  for (xmlHakija: Future[XMLHakija] <- hakijat.map(hakija2XMLHakija)) {
                                    xmlHakija pipeTo self
                                  }
                                  if (!fetching && sent == found) context.stop(self)
      case xmlHakija:XMLHakija => log.info("%s sending %s to %s" format (self, xmlHakija, streamer))
                                  streamer ! xmlHakija
                                  sent = sent + 1
                                  if (!fetching && sent == found) context.stop(self)
    }

    override def postStop() {
      streamer ! EOF
    }
  }
}
