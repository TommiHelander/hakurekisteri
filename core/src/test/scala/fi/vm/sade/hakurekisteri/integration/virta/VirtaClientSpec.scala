package fi.vm.sade.hakurekisteri.integration.virta


import akka.actor.ActorSystem
import com.ning.http.client.AsyncHttpClient
import fi.vm.sade.hakurekisteri.integration.{CapturingProvider, DispatchSupport, Endpoint}
import fi.vm.sade.hakurekisteri.test.tools.FutureWaiting
import org.mockito.Mockito
import org.scalatest.concurrent.AsyncAssertions
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object VirtaResults {

  val emptyResp = scala.io.Source.fromURL(getClass.getResource("/test-empty-response.xml")).mkString

  val multipleStudents = scala.io.Source.fromURL(getClass.getResource("/test-multiple-students-response.xml")).mkString

  val fault = scala.io.Source.fromURL(getClass.getResource("/test-fault.xml")).mkString

  val testResponse = scala.io.Source.fromURL(getClass.getResource("/test-response.xml")).mkString

  val opiskeluoikeustyypit = scala.io.Source.fromURL(getClass.getResource("/test-response-opiskeluoikeustyypit.xml")).mkString

}



class VirtaClientSpec extends FlatSpec with Matchers with AsyncAssertions with MockitoSugar with DispatchSupport with FutureWaiting {
  implicit val system = ActorSystem("test-virta-system")
  import Mockito._

  val endPoint = mock[Endpoint]

  when(endPoint.request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withBodyPart("1.2.4"))).thenReturn((200, List(), VirtaResults.emptyResp))
  when(endPoint.request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withBodyPart("1.2.5"))).thenReturn((500, List(), "Internal Server Error"))
  when(endPoint.request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withBodyPart("1.3.0"))).thenReturn((200, List(), VirtaResults.multipleStudents))
  when(endPoint.request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withBodyPart("1.5.0"))).thenReturn((500, List(), VirtaResults.fault))
  when(endPoint.request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withBodyPart("1.2.3"))).thenReturn((200, List(), VirtaResults.testResponse))
  when(endPoint.request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withBodyPart("1.2.9"))).thenReturn((200, List(), VirtaResults.opiskeluoikeustyypit))
  when(endPoint.request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withBodyPart("111111-1975"))).thenReturn((200, List(), VirtaResults.testResponse))

  val virtaClient = new VirtaClient(aClient = Some(new AsyncHttpClient(new CapturingProvider(endPoint))))

  behavior of "VirtaClient"

  it should "call Virta with provided oppijanumero" in {
    val oppijanumero = "1.2.3"
    val response = virtaClient.getOpiskelijanTiedot(oppijanumero = oppijanumero)

    waitFuture(response) {o => {
      verify(endPoint, atLeastOnce()).request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withBodyPart(s"<kansallinenOppijanumero>$oppijanumero</kansallinenOppijanumero>"))
      //httpClient.capturedRequestBody should include(s"<kansallinenOppijanumero>$oppijanumero</kansallinenOppijanumero>")
    }}
  }

  it should "call Virta with provided henkilotunnus" in {
    val hetu = "111111-1975"
    val response = virtaClient.getOpiskelijanTiedot(hetu = Some(hetu), oppijanumero = "1.2.3")

    waitFuture(response) {o => {
      verify(endPoint, atLeastOnce()).request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withBodyPart(s"<henkilotunnus>$hetu</henkilotunnus>"))

    }}
  }

  it should "wrap the operation in a SOAP envelope" in {
    val response = virtaClient.getOpiskelijanTiedot(oppijanumero = "1.2.3")

    waitFuture(response) {o => {
      verify(endPoint, atLeastOnce()).request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withBodyPart("<SOAP-ENV:Envelope"))

    }}
  }

  it should "attach Content-Type: text/xml header into the request" in {
    val response = virtaClient.getOpiskelijanTiedot(oppijanumero = "1.2.3")

    waitFuture(response) {o => {
      verify(endPoint, atLeastOnce()).request(forUrl("http://virtawstesti.csc.fi/luku/OpiskelijanTiedot").withHeader("Content-Type", "text/xml; charset=UTF-8"))
    }}
  }

  it should "return student information" in {
    val response: Future[Option[VirtaResult]] = virtaClient.getOpiskelijanTiedot(oppijanumero = "1.2.3")

    waitFuture(response) {o => {
      o.get.opiskeluoikeudet.size should be(2)
      o.get.tutkinnot.size should be(1)
    }}
  }

  it should "return None if no data is found" in {
    val response: Future[Option[VirtaResult]] = virtaClient.getOpiskelijanTiedot(oppijanumero = "1.2.4")

    waitFuture(response) {(o: Option[VirtaResult]) => {
      o should be(None)
    }}
  }

  it should "combine multiple student records into one opiskeluoikeus sequence" in {
    val response: Future[Option[VirtaResult]] = virtaClient.getOpiskelijanTiedot(oppijanumero = "1.3.0")

    waitFuture(response) {(o: Option[VirtaResult]) => {
      o.get.opiskeluoikeudet.size should be(3)
      o.get.tutkinnot.size should be(3)
    }}
  }

  it should "throw VirtaConnectionErrorException if an error occurred" in {

    intercept[VirtaConnectionErrorException] {
      val response = virtaClient.getOpiskelijanTiedot(oppijanumero = "1.2.5")
      Await.result(response, 10.seconds)

    }
  }

  it should "throw VirtaValidationError if validation error was returned" in {
    val response = virtaClient.getOpiskelijanTiedot(oppijanumero = "1.5.0")

    intercept[VirtaValidationError] {
      Await.result(response, 10.seconds)
    }
  }

  it should "parse only opiskeluoikeustyypit 1, 2, 3, 4, 5, 6 and 7" in {
    val response = virtaClient.getOpiskelijanTiedot(oppijanumero = "1.2.9")

    waitFuture(response)((o: Option[VirtaResult]) => {
      o.get.opiskeluoikeudet.size should be (2)
    })
  }
}
