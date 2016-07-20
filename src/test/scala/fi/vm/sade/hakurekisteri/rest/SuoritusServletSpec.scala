package fi.vm.sade.hakurekisteri.rest

import java.util.UUID

import akka.actor.{Actor, ActorSystem, Props}
import fi.vm.sade.hakurekisteri.acceptance.tools.FakeAuthorizer
import fi.vm.sade.hakurekisteri.integration.parametrit.IsRestrictionActive
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriJsonSupport
import fi.vm.sade.hakurekisteri.storage.repository.{InMemJournal, Updated}
import fi.vm.sade.hakurekisteri.suoritus._
import fi.vm.sade.hakurekisteri.tools.Peruskoulu
import fi.vm.sade.hakurekisteri.web.rest.support._
import fi.vm.sade.hakurekisteri.web.suoritus.SuoritusResource
import org.joda.time.LocalDate
import org.json4s.jackson.Serialization._
import org.scalatra.swagger.Swagger
import org.scalatra.test.scalatest.ScalatraFunSuite

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.implicitConversions

class SuoritusServletSpec extends ScalatraFunSuite {
  implicit val swagger: Swagger = new HakurekisteriSwagger
  val suoritus = Peruskoulu("1.2.3", "KESKEN", LocalDate.now,"1.2.4")
  implicit val system = ActorSystem("test-tuo-suoritus")
  implicit val ec: ExecutionContext = system.dispatcher

  implicit val security = new TestSecurity
  implicit def seq2journal[R <: fi.vm.sade.hakurekisteri.rest.support.Resource[UUID, R]](s:Seq[R]): InMemJournal[R, UUID] = {
    val journal = new InMemJournal[R, UUID]
    s.foreach((resource:R) => journal.addModification(Updated(resource.identify(UUID.randomUUID()))))
    journal
  }
  val suoritusRekisteri = system.actorOf(Props(new SuoritusActor(Seq(suoritus))))
  val guardedSuoritusRekisteri = system.actorOf(Props(new FakeAuthorizer(suoritusRekisteri)))
  val mockParameterActor = system.actorOf(Props(new Actor {
    override def receive: Actor.Receive = {
      case IsRestrictionActive(_) => sender ! true
    }
  }))
  addServlet(new SuoritusResource(guardedSuoritusRekisteri, mockParameterActor, SuoritusQuery(_)), "/*")

  test("get root should return 200") {
    get("/") {
      status should equal (200)
    }
  }

  implicit val formats = HakurekisteriJsonSupport.format

  test("save vahvistamaton suoritus should return vahvistettu:false") {
    post("/", write(VirallinenSuoritus("1.2.246.562.5.00000000001", "1.2.246.562.10.00000000001", "KESKEN", new LocalDate(), "1.2.246.562.24.00000000001", yksilollistaminen.Ei, "FI", None, false, "1.2.246.562.24.00000000001")), Map("Content-Type" -> "application/json; charset=utf-8")) {
      body should include ("\"vahvistettu\":false")
    }
  }

  test("save vahvistettu suoritus should return vahvistettu:true") {
    post("/", write(VirallinenSuoritus("1.2.246.562.5.00000000001", "1.2.246.562.10.00000000001", "KESKEN", new LocalDate(), "1.2.246.562.24.00000000001", yksilollistaminen.Ei, "FI", None, true, "1.2.246.562.24.00000000001")), Map("Content-Type" -> "application/json; charset=utf-8")) {
      body should include ("\"vahvistettu\":true")
    }
  }

  override def stop(): Unit = {
    system.shutdown()
    system.awaitTermination(15.seconds)
  }
}