package fi.vm.sade.hakurekisteri.rest

import akka.actor.{Actor, Props, ActorSystem}
import akka.pattern.pipe
import fi.vm.sade.hakurekisteri.PohjakoulutusOids
import fi.vm.sade.hakurekisteri.opiskelija.{Opiskelija, OpiskelijaHenkilotQuery}
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriJsonSupport
import fi.vm.sade.hakurekisteri.suoritus.{yksilollistaminen, VirallinenSuoritus, SuoritusHenkilotQuery}
import fi.vm.sade.hakurekisteri.web.permission.{PermissionCheckResponse, PermissionResource}
import fi.vm.sade.hakurekisteri.web.rest.support.HakurekisteriSwagger
import org.joda.time.{DateTime, LocalDate}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock.MockitoSugar
import org.scalatra.swagger.Swagger
import org.scalatra.test.scalatest.ScalatraFunSuite
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import org.json4s.jackson.Serialization._

class PermissionResourceSpec extends ScalatraFunSuite with MockitoSugar with BeforeAndAfterAll {

  implicit val system = ActorSystem("permission-test-system")
  implicit val format = HakurekisteriJsonSupport.format
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val swagger: Swagger = new HakurekisteriSwagger

  val suoritusActor = system.actorOf(Props(new Actor {
    override def receive: Receive = {
      case q: SuoritusHenkilotQuery if q.henkilot.contains("timeout") => Future.successful(Thread.sleep(2000)) pipeTo sender
      case q: SuoritusHenkilotQuery if q.henkilot.contains("rikki") => Future.failed(new Exception("palvelu rikki")) pipeTo sender
      case q: SuoritusHenkilotQuery => sender ! Seq(
        VirallinenSuoritus(PohjakoulutusOids.perusopetus, "1.2.246.562.10.1", "VALMIS", new LocalDate(), "1.2.246.562.24.1", yksilollistaminen = yksilollistaminen.Ei, "FI", None, vahv = true, "1.2.246.562.24.10")
      )
    }
  }))
  val opiskelijaActor = system.actorOf(Props(new Actor {
    override def receive: Actor.Receive = {
      case q: OpiskelijaHenkilotQuery => sender ! Seq(
        Opiskelija("1.2.246.562.10.2", "9", "9A", "1.2.246.562.24.1", new DateTime(), None, "1.2.246.562.24.10")
      )
    }
  }))

  addServlet(new PermissionResource(suoritusActor, opiskelijaActor, Some(1.seconds)), "/")

  test("should return http status 200") {
    val json =
      """{
        |  "personOidsForSamePerson": ["1.2.246.562.24.1"],
        |  "organisationOids": ["1.2.246.562.10.1"]
        |}""".stripMargin

    post("/", json) {
      response.status should be (200)
    }
  }

  test("should return true if matching suoritus found") {
    val json =
      """{
        |  "personOidsForSamePerson": ["1.2.246.562.24.1"],
        |  "organisationOids": ["1.2.246.562.10.1"]
        |}""".stripMargin

    post("/", json) {
      val checkResponse = read[PermissionCheckResponse](response.body)
      checkResponse.accessAllowed should be (Some(true))
    }
  }

  test("should return true if matching opiskelija found") {
    val json =
      """{
        |  "personOidsForSamePerson": ["1.2.246.562.24.1"],
        |  "organisationOids": ["1.2.246.562.10.2"]
        |}""".stripMargin

    post("/", json) {
      val checkResponse = read[PermissionCheckResponse](response.body)
      checkResponse.accessAllowed should be (Some(true))
    }
  }

  test("should return false if no matching suoritus or opiskelija found") {
    val json =
      """{
        |  "personOidsForSamePerson": ["1.2.246.562.24.1"],
        |  "organisationOids": ["1.2.246.562.10.3"]
        |}""".stripMargin

    post("/", json) {
      val checkResponse = read[PermissionCheckResponse](response.body)
      checkResponse.accessAllowed should be (Some(false))
    }
  }

  test("should return http status 500 if an error occurred") {
    val json =
      """{
        |  "personOidsForSamePerson": ["rikki"],
        |  "organisationOids": []
        |}""".stripMargin

    post("/", json) {
      response.status should be (500)
    }
  }

  test("should return errorMessage if an error occurred") {
    val json =
      """{
        |  "personOidsForSamePerson": ["rikki"],
        |  "organisationOids": ["foo"]
        |}""".stripMargin

    post("/", json) {
      val checkResponse = read[PermissionCheckResponse](response.body)
      checkResponse.errorMessage should be (Some("palvelu rikki"))
    }
  }

  test("should return specific errorMessage if an error occurred") {
    val json =
      """{
        |  "personOidsForSamePerson": ["rikki"],
        |  "organisationOids": ["foo"]
        |}""".stripMargin

    post("/", json) {
      val checkResponse = read[PermissionCheckResponse](response.body)
      checkResponse.errorMessage should be (Some("palvelu rikki"))
    }
  }

  test("should return timeout errorMessage if an error occurred") {
    val json =
      """{
        |  "personOidsForSamePerson": ["timeout"],
        |  "organisationOids": ["foo"]
        |}""".stripMargin

    post("/", json) {
      val checkResponse = read[PermissionCheckResponse](response.body)
      checkResponse.errorMessage should be (Some("timeout occurred during permission check"))
    }
  }

  test("should return http 400 if cannot parse request object") {
    post("/", "") {
      response.status should be (400)
    }
  }

  override def afterAll() = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

}
