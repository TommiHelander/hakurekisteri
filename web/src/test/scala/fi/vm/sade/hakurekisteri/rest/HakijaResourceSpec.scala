package fi.vm.sade.hakurekisteri.rest

import akka.actor.{Actor, Props}
import fi.vm.sade.hakurekisteri.acceptance.tools.HakeneetSupport
import fi.vm.sade.hakurekisteri.hakija._
import fi.vm.sade.hakurekisteri.integration.koodisto._
import fi.vm.sade.hakurekisteri.web.hakija.HakijaResource
import fi.vm.sade.hakurekisteri.web.rest.support.{HakurekisteriSwagger, TestSecurity}
import org.scalatra.swagger.Swagger
import org.scalatra.test.scalatest.ScalatraFunSuite

class HakijaResourceSpec extends ScalatraFunSuite with HakeneetSupport {
  implicit val swagger: Swagger = new HakurekisteriSwagger
  implicit val security = new TestSecurity
  val hakijat = system.actorOf(Props(new HakijaActor(hakupalvelu, organisaatioActor, koodistoActor, sijoittelu)))
  addServlet(new HakijaResource(hakijat), "/")

  test("result is XML") {
    get("/?hakuehto=Kaikki&tyyppi=Xml") {
      body should include ("<?xml")
    }
  }

  test("XML contains root element") {
    get("/?hakuehto=Kaikki&tyyppi=Xml") {
      body should include ("<Hakijat")
    }
  }

  test("JSON contains postoffice") {
    hakupalvelu has (FullHakemus1)
    get("/?hakuehto=Kaikki&tyyppi=Json") {
      body should include ("postitoimipaikka")
    }
  }

  test("result is binary and not empty when asked in Excel") {
    get("/?hakuehto=Kaikki&tyyppi=Excel") {
      body.length should not be 0
      header("Content-Type") should include ("application/octet-stream")
      header("Content-Disposition") should be ("attachment;filename=hakijat.xls")
    }
  }

  override def stop(): Unit = {
    import scala.concurrent.duration._
    system.shutdown()
    system.awaitTermination(15.seconds)
    super.stop()
  }

}
