package fi.vm.sade.hakurekisteri.rest

import akka.actor.{Actor, ActorSystem, Props}
import fi.vm.sade.hakurekisteri.Config
import fi.vm.sade.hakurekisteri.ensikertalainen.{Ensikertalainen, EnsikertalainenActor}
import fi.vm.sade.hakurekisteri.integration.hakemus.{Hakemus, HakemusQuery}
import fi.vm.sade.hakurekisteri.integration.tarjonta.{GetKomoQuery, KomoResponse}
import fi.vm.sade.hakurekisteri.integration.valintarekisteri.{EnsimmainenVastaanotto, ValintarekisteriQuery}
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriJsonSupport
import fi.vm.sade.hakurekisteri.suoritus.SuoritusHenkilotQuery
import fi.vm.sade.hakurekisteri.web.ensikertalainen.EnsikertalainenResource
import fi.vm.sade.hakurekisteri.web.rest.support.{HakurekisteriSwagger, TestSecurity}
import org.joda.time.DateTime
import org.scalatra.test.scalatest.ScalatraFunSuite
import org.json4s.jackson.Serialization._

import scala.concurrent.ExecutionContext

class EnsikertalainenResourceSpec extends ScalatraFunSuite {

  implicit val system = ActorSystem("ensikertalainen-resource-test-system")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val security = new TestSecurity
  implicit val swagger = new HakurekisteriSwagger
  implicit val formats = HakurekisteriJsonSupport.format

  val vastaanottohetki = new DateTime(2015, 1, 1, 0, 0, 0, 0)

  addServlet(new EnsikertalainenResource(ensikertalainenActor = system.actorOf(Props(new EnsikertalainenActor(
    suoritusActor = system.actorOf(Props(new Actor {
      override def receive: Receive = {
        case q: SuoritusHenkilotQuery =>
          sender ! Seq()
      }
    })),
    valintarekisterActor = system.actorOf(Props(new Actor {
      override def receive: Actor.Receive = {
        case q: ValintarekisteriQuery =>
          val map: Seq[EnsimmainenVastaanotto] = q.henkiloOids.toSeq.map(o => EnsimmainenVastaanotto(o, Some(vastaanottohetki)))
          sender ! map
      }
    })),
    tarjontaActor = system.actorOf(Props(new Actor {
      override def receive: Actor.Receive = {
        case q: GetKomoQuery => sender ! KomoResponse(q.oid, None)
      }
    })),
    config = Config.mockConfig
  ))), hakemusRekisteri = system.actorOf(Props(new Actor {
      override def receive: Actor.Receive = {
        case q: HakemusQuery if q.haku.isDefined => sender ! Seq(
          Hakemus().setPersonOid("foo").build,
          Hakemus().setPersonOid("bar").build,
          Hakemus().setPersonOid("zap").build
        )
      }
    }))), "/ensikertalainen")

  test("returns 200 ok") {
    get("/ensikertalainen?henkilo=foo") {
      response.status should be (200)
    }
  }

  test("returns ensikertalainen false") {
    get("/ensikertalainen?henkilo=foo") {
      read[Ensikertalainen](response.body).ensikertalainen should be (false)
    }
  }

  test("returns ensikertalaisuus lost by KkVastaanotto") {
    get("/ensikertalainen?henkilo=foo") {
      val e = read[Ensikertalainen](response.body)
      e.menettamisenPeruste.map(_.peruste) should be (Some("KkVastaanotto"))
      e.menettamisenPeruste.map(_.paivamaara.toString) should be (Some(vastaanottohetki.toString))
    }
  }

  test("returns a sequence of ensikertalaisuus") {
    post("/ensikertalainen", """["foo", "bar", "foo"]""") {
      val e = read[Seq[Ensikertalainen]](response.body)
      e.size should be (2)
    }
  }

  test("returns ensikertalaisuus for all hakijas in haku") {
    get("/ensikertalainen/haku/1.2.3") {
      val e = read[Seq[Ensikertalainen]](response.body)
      e.size should be (3)
    }
  }

  override def stop(): Unit = {
    import scala.concurrent.duration._
    system.shutdown()
    system.awaitTermination(15.seconds)
    super.stop()
  }

}
