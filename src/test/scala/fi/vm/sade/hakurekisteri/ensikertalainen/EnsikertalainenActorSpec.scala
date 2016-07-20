package fi.vm.sade.hakurekisteri.ensikertalainen

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import fi.vm.sade.hakurekisteri.test.tools.FutureWaiting
import fi.vm.sade.hakurekisteri.Config
import fi.vm.sade.hakurekisteri.dates.Ajanjakso
import fi.vm.sade.hakurekisteri.integration.hakemus.{FullHakemus, Hakemus, HakemusQuery}
import fi.vm.sade.hakurekisteri.integration.haku.{GetHaku, Haku, Kieliversiot}
import fi.vm.sade.hakurekisteri.integration.tarjonta.{GetKomoQuery, KomoResponse}
import fi.vm.sade.hakurekisteri.integration.valintarekisteri.{EnsimmainenVastaanotto, ValintarekisteriQuery}
import fi.vm.sade.hakurekisteri.opiskeluoikeus.{Opiskeluoikeus, OpiskeluoikeusHenkilotQuery}
import fi.vm.sade.hakurekisteri.suoritus._
import org.joda.time.{DateTime, LocalDate}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.language.reflectiveCalls

class EnsikertalainenActorSpec extends FlatSpec with Matchers with FutureWaiting with BeforeAndAfterAll {

  implicit val system = ActorSystem("ensikertalainen-test-system")
  implicit val timeout: Timeout = 10.seconds

  behavior of "EnsikertalainenActor"

  private val henkiloOid: String = "1.2.246.562.24.1"
  private val myontaja: String = "1.2.246.562.10.1"
  private val koulutus_699999: String = "koulutus_699999"

  it should "return true if no kk tutkinto and no vastaanotto found" in {
    val (actor, _) = initEnsikertalainenActor()

    waitFuture((actor ? EnsikertalainenQuery(henkiloOids = Set(henkiloOid), hakuOid = Testihaku.oid)).mapTo[Seq[Ensikertalainen]])(e => {
      e.head.ensikertalainen should be(true)
    })
  }

  it should "return ensikertalainen false based on kk tutkinto" in {
    val date = new LocalDate()
    val (actor, valintarek) = initEnsikertalainenActor(
      suoritukset = Seq(
        VirallinenSuoritus(koulutus_699999, myontaja, "VALMIS", date, henkiloOid, yksilollistaminen.Ei, "FI", None, vahv = true, "")
      ),
      opiskeluoikeudet = Seq(
        Opiskeluoikeus(new LocalDate(), None, henkiloOid, koulutus_699999, myontaja, "")
      ),
      vastaanotot = Seq(EnsimmainenVastaanotto(henkiloOid, Some(new DateTime())))
    )

    waitFuture(
      (actor ? EnsikertalainenQuery(henkiloOids = Set(henkiloOid), hakuOid = Testihaku.oid)).mapTo[Seq[Ensikertalainen]]
    )((e: Seq[Ensikertalainen]) => {
      e.head.ensikertalainen should be (false)
      e.head.menettamisenPeruste should be (Some(SuoritettuKkTutkinto(date.toDateTimeAtStartOfDay)))
      valintarek.underlyingActor.counter should be (0)
    })
  }

  it should "return ensikertalainen false based on opiskeluoikeus" in {
    val date = new LocalDate()
    val (actor, valintarek) = initEnsikertalainenActor(
      opiskeluoikeudet = Seq(
        Opiskeluoikeus(date, Some(date.plusYears(1)), henkiloOid, koulutus_699999, myontaja, "")
      ),
      vastaanotot = Seq(EnsimmainenVastaanotto(henkiloOid, Some(date.toDateTimeAtStartOfDay)))
    )

    waitFuture(
      (actor ? EnsikertalainenQuery(henkiloOids = Set(henkiloOid), hakuOid = Testihaku.oid)).mapTo[Seq[Ensikertalainen]]
    )((e: Seq[Ensikertalainen]) => {
      e.head.ensikertalainen should be (false)
      e.head.menettamisenPeruste should be (Some(OpiskeluoikeusAlkanut(date.toDateTimeAtStartOfDay)))
      valintarek.underlyingActor.counter should be (0)
    })
  }

  it should "return ensikertalaisuus false based on hakemus" in {
    val date = new LocalDate()
    val vanhatutkinto = 1990
    val (actor, valintarek) = initEnsikertalainenActor(
      suoritukset = Seq(
        VirallinenSuoritus(koulutus_699999, myontaja, "VALMIS", new LocalDate(), henkiloOid, yksilollistaminen.Ei, "FI", None, vahv = true, "")
      ),
      opiskeluoikeudet = Seq(
        Opiskeluoikeus(date, None, henkiloOid, koulutus_699999, myontaja, "")
      ),
      hakemukset = Seq(Hakemus().setPersonOid(henkiloOid).setSuorittanutSuomalaisenKkTutkinnon(vanhatutkinto).build),
      vastaanotot = Seq(EnsimmainenVastaanotto(henkiloOid, Some(date.toDateTimeAtCurrentTime)))
    )

    waitFuture(
      (actor ? EnsikertalainenQuery(henkiloOids = Set(henkiloOid), hakuOid = Testihaku.oid)).mapTo[Seq[Ensikertalainen]]
    )((e: Seq[Ensikertalainen]) => {
      e.head.ensikertalainen should be (false)
      e.head.menettamisenPeruste should be (Some(SuoritettuKkTutkintoHakemukselta(vanhatutkinto)))
      valintarek.underlyingActor.counter should be (0)
    })
  }

  it should "return ensikertalaisuus true based on hakemus" in {
    val (actor, valintarek) = initEnsikertalainenActor(
      hakemukset = Seq(Hakemus().setApplicationSystemId(Testihaku.oid).build)
    )

    waitFuture(
      (actor ? EnsikertalainenQuery(henkiloOids = Set(henkiloOid), hakuOid = Testihaku.oid)).mapTo[Seq[Ensikertalainen]]
    )((e: Seq[Ensikertalainen]) => {
      e.head.ensikertalainen should be (true)
      valintarek.underlyingActor.counter should be (1)
    })
  }

  it should "return ensikertalainen false based on vastaanotto" in {
    val date = new DateTime()
    val (actor, valintarek) = initEnsikertalainenActor(
      vastaanotot = Seq(EnsimmainenVastaanotto(henkiloOid, Some(date)))
    )

    waitFuture(
      (actor ? EnsikertalainenQuery(henkiloOids = Set(henkiloOid), hakuOid = Testihaku.oid)).mapTo[Seq[Ensikertalainen]]
    )((e: Seq[Ensikertalainen]) => {
      e.head.ensikertalainen should be (false)
      e.head.menettamisenPeruste should be (Some(KkVastaanotto(date)))
      valintarek.underlyingActor.counter should be (1)
    })
  }

  private def initEnsikertalainenActor(suoritukset: Seq[Suoritus] = Seq(),
                                       opiskeluoikeudet: Seq[Opiskeluoikeus] = Seq(),
                                       vastaanotot: Seq[EnsimmainenVastaanotto] = Seq(),
                                       hakemukset: Seq[FullHakemus] = Seq()) = {
    val valintarekisteri = TestActorRef(new Actor {
      var counter = 0
      override def receive: Actor.Receive = {
        case q: ValintarekisteriQuery =>
          counter = counter + 1
          sender ! vastaanotot
      }
    })
    (system.actorOf(Props(new EnsikertalainenActor(
      suoritusActor = system.actorOf(Props(new Actor {
        override def receive: Receive = {
          case q: SuoritusHenkilotQuery =>
            sender ! suoritukset
        }
      })),
      opiskeluoikeusActor = system.actorOf(Props(new Actor {
        override def receive: Receive = {
          case q: OpiskeluoikeusHenkilotQuery =>
            sender ! opiskeluoikeudet
        }
      })),
      valintarekisterActor = valintarekisteri,
      tarjontaActor = system.actorOf(Props(new Actor {
        override def receive: Actor.Receive = {
          case q: GetKomoQuery => sender ! KomoResponse(q.oid, None)
        }
      })),
      config = Config.mockConfig,
      hakuActor = system.actorOf(Props(new Actor {
        override def receive: Receive = {
          case q: GetHaku => sender ! Testihaku
        }
      })),
      hakemusActor = system.actorOf(Props(new Actor {
        override def receive: Receive = {
          case q: HakemusQuery => sender ! hakemukset
        }
      }))
    ))), valintarekisteri)
  }

  override def afterAll() {
    system.shutdown()
    system.awaitTermination(15.seconds)
  }

}

object Testihaku extends Haku(
  nimi = Kieliversiot(Some("haku 1"), Some("haku 1"), Some("haku 1")),
  oid = "1.2.3.4",
  aika = Ajanjakso(new LocalDate(), Some(new LocalDate().plusMonths(1))),
  kausi = "K",
  vuosi = new LocalDate().getYear,
  koulutuksenAlkamiskausi = Some("S"),
  koulutuksenAlkamisvuosi = Some(new LocalDate().getYear),
  kkHaku = true,
  viimeinenHakuaikaPaattyy = Some(new DateTime().plusDays(1))
)