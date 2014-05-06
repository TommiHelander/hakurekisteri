package fi.vm.sade.hakurekisteri.acceptance.tools

import fi.vm.sade.hakurekisteri.hakija._
import fi.vm.sade.hakurekisteri.hakija
import org.scalatra.swagger.Swagger
import fi.vm.sade.hakurekisteri.rest.support.{User, HakurekisteriJsonSupport, HakurekisteriSwagger}
import akka.actor.{Actor, Props, ActorSystem}
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import fi.vm.sade.hakurekisteri.hakija.HakijaQuery
import fi.vm.sade.hakurekisteri.hakija.XMLHakemus
import fi.vm.sade.hakurekisteri.hakija.XMLHakutoive
import scala.Some
import org.scalatest.Suite
import org.scalatra.test.HttpComponentsClient
import akka.actor.Status.Success
import akka.actor.FSM.Failure
import scala.concurrent.{Promise, Future, ExecutionContext}
import fi.vm.sade.hakurekisteri.hakija.HakijaResource.EOF
import akka.event.Logging
import java.util.UUID

trait HakeneetSupport extends Suite with HttpComponentsClient with HakurekisteriJsonSupport {

  object OppilaitosX extends Organisaatio("1.10.1", Map("fi" -> "Oppilaitos X"), None, Some("00001"), None)
  object OppilaitosY extends Organisaatio("1.10.2", Map("fi" -> "Oppilaitos Y"), None, Some("00002"), None)

  object OpetuspisteX extends Organisaatio("1.10.3", Map("fi" -> "Opetuspiste X"), Some("0000101"), None, Some("1.10.1"))
  object OpetuspisteZ extends Organisaatio("1.10.5", Map("fi" -> "Opetuspiste Z"), Some("0000101"), None, Some("1.10.1"))
  object OpetuspisteY extends Organisaatio("1.10.4", Map("fi" -> "Opetuspiste Y"), Some("0000201"), None, Some("1.10.2"))

  object FullHakemus1 extends FullHakemus("1.25.1", None, "1.1",
    Some(Map(
      "henkilotiedot" -> Map(
        "kansalaisuus" -> "FIN",
        "asuinmaa" -> "FIN",
        "matkapuhelinnumero1" -> "0401234567",
        "Sukunimi" -> "Mäkinen",
        "Henkilotunnus" -> "200394-9839",
        "Postinumero" -> "00100",
        "lahiosoite" -> "Katu 1",
        "sukupuoli" -> "1",
        "Sähköposti" -> "mikko@testi.oph.fi",
        "Kutsumanimi" -> "Mikko",
        "Etunimet" -> "Mikko",
        "kotikunta" -> "098",
        "aidinkieli" -> "FI",
        "syntymaaika" -> "20.03.1994",
        "onkoSinullaSuomalainenHetu" -> "true"),
      "koulutustausta" -> Map(
        "PK_PAATTOTODISTUSVUOSI" -> "2014",
        "POHJAKOULUTUS" -> "1",
        "perusopetuksen_kieli" -> "FI",
        "lahtokoulu" -> OppilaitosX.oid,
        "lahtoluokka" -> "9A",
        "luokkataso" -> "9"),
      "hakutoiveet" -> Map(
        "preference2-Opetuspiste" -> "Ammattikoulu Lappi2",
        "preference2-Opetuspiste-id" -> "1.10.4",
        "preference2-Koulutus" -> "Musiikin koulutusohjelma, pk (Musiikkialan perustutkinto)4",
        "preference2-Koulutus-id" -> "1.11.2",
        "preference2-Koulutus-id-aoIdentifier" -> "460",
        "preference2-Koulutus-id-educationcode" -> "koulutus_321204",
        "preference2-Koulutus-id-lang" -> "FI",
        "preference1-Opetuspiste" -> "Ammattikoulu Lappi",
        "preference1-Opetuspiste-id" -> "1.10.3",
        "preference1-Koulutus" -> "Musiikin koulutusohjelma, pk (Musiikkialan perustutkinto)",
        "preference1-Koulutus-id" -> "1.11.1",
        "preference1-Koulutus-id-aoIdentifier" -> "460",
        "preference1-Koulutus-id-educationcode" -> "koulutus_321204",
        "preference1-Koulutus-id-lang" -> "FI"),
      "lisatiedot" -> Map(
        "lupaMarkkinointi" -> "true",
        "lupaJulkaisu" -> "true")))
  )
  object FullHakemus2 extends FullHakemus("1.25.2", Some("1.24.2"), "1.2",
    Some(Map(
      "henkilotiedot" -> Map(
        "kansalaisuus" -> "FIN",
        "asuinmaa" -> "FIN",
        "matkapuhelinnumero1" -> "0401234568",
        "Sukunimi" -> "Virtanen",
        "Henkilotunnus" -> "200394-959H",
        "Postinumero" -> "00100",
        "lahiosoite" -> "Katu 2",
        "sukupuoli" -> "1",
        "Sähköposti" -> "ville@testi.oph.fi",
        "Kutsumanimi" -> "Ville",
        "Etunimet" -> "Ville",
        "kotikunta" -> "098",
        "aidinkieli" -> "FI",
        "syntymaaika" -> "20.03.1994",
        "onkoSinullaSuomalainenHetu" -> "true"),
      "koulutustausta" -> Map(
        "PK_PAATTOTODISTUSVUOSI" -> "2014",
        "POHJAKOULUTUS" -> "1",
        "perusopetuksen_kieli" -> "FI",
        "lahtokoulu" -> OppilaitosY.oid,
        "lahtoluokka" -> "9A",
        "luokkataso" -> "9"),
      "hakutoiveet" -> Map(
        "preference2-Opetuspiste" -> "Ammattiopisto Loppi2\"",
        "preference2-Opetuspiste-id" -> "1.10.5",
        "preference2-Koulutus" -> "Musiikin koulutusohjelma, pk (Musiikkialan perustutkinto)2",
        "preference2-Koulutus-id" -> "1.11.1",
        "preference2-Koulutus-id-aoIdentifier" -> "460",
        "preference2-Koulutus-id-educationcode" -> "koulutus_321204",
        "preference2-Koulutus-id-lang" -> "FI",
        "preference1-Opetuspiste" -> "Ammattiopisto Loppi",
        "preference1-Opetuspiste-id" -> "1.10.4",
        "preference1-Koulutus" -> "Musiikin koulutusohjelma, pk (Musiikkialan perustutkinto)",
        "preference1-Koulutus-id" -> "1.11.2",
        "preference1-Koulutus-id-aoIdentifier" -> "460",
        "preference1-Koulutus-id-educationcode" -> "koulutus_321204",
        "preference1-Koulutus-id-lang" -> "FI"),
      "lisatiedot" -> Map(
        "lupaMarkkinointi" -> "true",
        "lupaJulkaisu" -> "true")))
  )

  object notEmpty

  implicit def fullHakemus2SmallHakemus(h: FullHakemus): ListHakemus = {
    ListHakemus(h.oid)
  }

  import _root_.akka.pattern.ask

  implicit val system = ActorSystem()
  implicit def executor: ExecutionContext = system.dispatcher
  implicit val defaultTimeout = Timeout(60, TimeUnit.SECONDS)


  object hakupalvelu extends Hakupalvelu {

    val maxApplications = 50000000
    var tehdytHakemukset: Seq[FullHakemus] = Seq()


    
    
    override def getHakijat(q: HakijaQuery, page: Int = 0): Future[Seq[Hakija]] = (q.organisaatio, page) match {
      case (_ , i) if i > 0 => Future.successful(Seq())
      case (Some(org), _) => {
        println("Haetaan tarjoajalta %s".format(org))
        Future(hakijat.filter(_.hakemus.hakutoiveet.exists(_.hakukohde.koulutukset.exists((kohde) => {println(kohde);kohde.tarjoaja == org}))))
      }
      case _ => Future(hakijat)
      
    }


    def hakijat: Seq[Hakija] = {
      tehdytHakemukset.map(RestHakupalvelu.getHakija(_))
    }

    def find(q: HakijaQuery): Future[Seq[ListHakemus]] = q.organisaatio match {
      case Some(OpetuspisteX.oid) => Future(Seq(FullHakemus1))
      case Some(OpetuspisteY.oid) => Future(Seq(FullHakemus2))
      case None => Future(Seq(FullHakemus1, FullHakemus2))
    }

    def get(hakemusOid: String, user: Option[User]): Future[Option[FullHakemus]] = hakemusOid match {
      case "1.25.1" => Future(Some(FullHakemus1))
      case "1.25.2" => Future(Some(FullHakemus2))
      case default => Future(None)
    }

    def is(token:Any) = token match {
      case notEmpty => has(FullHakemus1, FullHakemus2)
    }

    def has(hakemukset: FullHakemus*) = {
      tehdytHakemukset = hakemukset
    }

  }

  object organisaatiopalvelu extends Organisaatiopalvelu {
    override def getAll() =  Future.successful(Seq(OppilaitosX,OppilaitosY,OpetuspisteX, OpetuspisteY).map(_.oid))

    override def get(str: String): Future[Option[Organisaatio]] = Future(doTheMatch(str))

    def doTheMatch(str: String): Option[Organisaatio] = str match {
      case OppilaitosX.oid => Some(OppilaitosX)
      case OppilaitosY.oid => Some(OppilaitosY)
      case OpetuspisteX.oid => Some(OpetuspisteX)
      case OpetuspisteY.oid => Some(OpetuspisteY)
      case default => None
    }
  }

  object koodistopalvelu extends Koodistopalvelu {
    override def getRinnasteinenKoodiArvo(koodiUri: String, rinnasteinenKoodistoUri: String): Future[String] = koodiUri match {
      case _ => Future("246")
    }
  }

  object hakijaResource {
    implicit val swagger: Swagger = new HakurekisteriSwagger


    val orgAct = system.actorOf(Props(new OrganisaatioActor(organisaatiopalvelu)))
    val hakijaActor = system.actorOf(Props(new HakijaActor(hakupalvelu, orgAct, koodistopalvelu)), "test-hakuactor")

    import scala.concurrent.promise

    def get(q: HakijaQuery) = {
      val prom = promise[XMLHakijat]
      system.actorOf(Props(new TestStreamer(prom, q)), "test-streamer-%s" format UUID.randomUUID)
      prom.future
    }

    class TestStreamer(result: Promise[XMLHakijat], q: HakijaQuery) extends Actor {

      val log = Logging(context.system, this)

      var renderables:Seq[XMLHakija] = Seq()



      override def preStart() {
        log.info(self + " sending query " + q + " to " + hakijaActor)
        hakijaActor ! q
      }

      override def receive = {

          case EOF =>   log.info("received eof")
                        result.success(XMLHakijat(renderables))
                        context.stop(self)
          case a:XMLHakija =>  log.info("received" + a)
                               renderables = renderables ++ Seq(a)
          case a => log.info("strangely " + a)

      }
    }
  }
}
