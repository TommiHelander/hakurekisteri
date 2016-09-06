package fi.vm.sade.hakurekisteri.rest

import akka.actor.ActorSystem
import fi.vm.sade.hakurekisteri.integration.{ExecutorUtil, Endpoint, DispatchSupport}
import fi.vm.sade.hakurekisteri.web.integration.virta.VirtaSuoritusResource
import fi.vm.sade.hakurekisteri.web.integration.ytl.YtlResource
import fi.vm.sade.hakurekisteri.web.rest.support.{Security, HakurekisteriSwagger}
import org.mockito.Mockito
import org.scalatest.mock.MockitoSugar
import org.scalatra.swagger.Swagger
import org.scalatra.test.scalatest.ScalatraFunSuite

class YtlResourceSpec extends ScalatraFunSuite with DispatchSupport with MockitoSugar {
  implicit val system = ActorSystem()
  implicit val clientEc = ExecutorUtil.createExecutor(1, "ytl-resource-test-pool")
  implicit val swagger: Swagger = new HakurekisteriSwagger
  implicit val adminSecurity: Security = new SuoritusResourceAdminTestSecurity

  import Mockito._

  addServlet(new YtlResource(null), "/*")

  val endPoint = mock[Endpoint]

  test("should launch YTL fetch") {
    get("/http_request") {
      status should be (202)
      //body should be ("{\"oppijanumero\":\"1.2.4\",\"opiskeluoikeudet\":[],\"tutkinnot\":[],\"suoritukset\":[]}")
    }
    get("/http_request/050996-9574") {
      status should be (202)
      //body should be ("{\"oppijanumero\":\"1.2.4\",\"opiskeluoikeudet\":[],\"tutkinnot\":[],\"suoritukset\":[]}")
    }
    get("/http_request/") {
      status should be (202)
      //body should be ("{\"oppijanumero\":\"1.2.4\",\"opiskeluoikeudet\":[],\"tutkinnot\":[],\"suoritukset\":[]}")
    }
  }
}
