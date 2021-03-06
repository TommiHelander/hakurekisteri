package fi.vm.sade.hakurekisteri.integration.organisaatio

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.ning.http.client.AsyncHttpClient
import fi.vm.sade.hakurekisteri.{Config, MockConfig}
import fi.vm.sade.hakurekisteri.integration._
import org.mockito.Mockito._
import org.scalatest.Matchers
import org.scalatest.concurrent.AsyncAssertions
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Span}
import org.scalatra.test.scalatest.ScalatraFunSuite

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class OrganisaatioActorSpec extends ScalatraFunSuite with Matchers with AsyncAssertions with MockitoSugar with DispatchSupport with ActorSystemSupport with LocalhostProperties {

  implicit val timeout: Timeout = 60.seconds
  val organisaatioConfig = ServiceConfig(serviceUrl = "http://localhost/organisaatio-service")

  def createEndPoint(implicit ec: ExecutionContext) = {
    val e = mock[Endpoint]

    when(e.request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&suunnitellut=true"))).thenReturn((200, List(), OrganisaatioResults.hae))
    when(e.request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/99999"))).thenReturn((200, List(), OrganisaatioResults.ysiysiysiysiysi))
    when(e.request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/05127"))).thenReturn((200, List(), OrganisaatioResults.pikkola))
    when(e.request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/1.2.246.562.10.16546622305"))).thenReturn((200, List(), OrganisaatioResults.pikkola))

    e
  }



  test("OrganisaatioActor should contain all organisaatios after startup") {
    withSystem(
      implicit system => {
        implicit val ec = system.dispatcher
        val (endPoint, organisaatioActor) = initOrganisaatioActor()

        waitFuture((organisaatioActor ? Oppilaitos("05127")).mapTo[OppilaitosResponse])(o => {
          o.oppilaitos.oppilaitosKoodi.get should be ("05127")
        })

        verify(endPoint, times(1)).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&suunnitellut=true"))
        verify(endPoint, times(0)).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/05127"))
      }
    )
  }

  test("OrganisaatioActor should return organisaatio from cache using organisaatio oid") {
    withSystem(
      implicit system => {
        implicit val ec = system.dispatcher
        val (endPoint, organisaatioActor) = initOrganisaatioActor()

        waitFuture((organisaatioActor ? "1.2.246.562.10.16546622305").mapTo[Option[Organisaatio]])(o => {
          o.get.oppilaitosKoodi.get should be ("05127")
        })

        verify(endPoint, times(1)).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&suunnitellut=true"))
        verify(endPoint, times(0)).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/1.2.246.562.10.16546622305"))
      }
    )
  }

  test("OrganisaatioActor should return organisaatio from cache using oppilaitoskoodi") {
    withSystem(
      implicit system => {
        implicit val ec = system.dispatcher
        val (endPoint, organisaatioActor) = initOrganisaatioActor()

        waitFuture((organisaatioActor ? Oppilaitos("05127")).mapTo[OppilaitosResponse])(o => {
          o.oppilaitos.oppilaitosKoodi.get should be ("05127")
        })

        verify(endPoint, times(1)).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&suunnitellut=true"))
        verify(endPoint, times(0)).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/05127"))
      }
    )
  }

  test("OrganisaatioActor should find organisaatio from organisaatio-service if not found in cache") {
    withSystem(
      implicit system => {
        implicit val ec = system.dispatcher
        val (endPoint, organisaatioActor) = initOrganisaatioActor()

        waitFuture((organisaatioActor ? Oppilaitos("99999")).mapTo[OppilaitosResponse])(o => {
          o.oppilaitos.oppilaitosKoodi.get should be ("99999")
        })

        verify(endPoint, times(1)).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&suunnitellut=true"))
        verify(endPoint, times(1)).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/99999"))
      }
    )
  }

  test("OrganisaatioActor should cache a single result") {
    withSystem(
      implicit system => {
        implicit val ec = system.dispatcher
        val (endPoint, organisaatioActor) = initOrganisaatioActor()

        organisaatioActor ! Oppilaitos("99999")

        Thread.sleep(100)

        waitFuture((organisaatioActor ? Oppilaitos("99999")).mapTo[OppilaitosResponse])(o => {
          o.oppilaitos.oppilaitosKoodi.get should be ("99999")
        })

        verify(endPoint, times(1)).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&suunnitellut=true"))
        verify(endPoint, times(1)).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/99999"))
      }
    )
  }

  test("OrganisaatioActor should refresh cache") {
    withSystem(
      implicit system => {
        implicit val ec = system.dispatcher
        val (endPoint, organisaatioActor) = initOrganisaatioActor(Some(2.seconds))

        Thread.sleep(1500)

        verify(endPoint, atLeastOnce()).request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&suunnitellut=true"))
      }
    )
  }

  def initOrganisaatioActor(ttl: Option[FiniteDuration] = None)(implicit system: ActorSystem, ec: ExecutionContext): (Endpoint, ActorRef) = {
    val endPoint = createEndPoint
    val organisaatioActor = system.actorOf(Props(new HttpOrganisaatioActor(new VirkailijaRestClient(config = organisaatioConfig, aClient = Some(new AsyncHttpClient(new CapturingProvider(endPoint)))), new MockConfig, initDuringStartup = false, ttl)))

    Await.result((organisaatioActor ? RefreshOrganisaatioCache).mapTo[Boolean], Duration(10, TimeUnit.SECONDS))

    (endPoint, organisaatioActor)
  }

  def waitFuture[A](f: Future[A])(assertion: A => Unit)(implicit ec: ExecutionContext) = {
    val w = new Waiter

    f.onComplete(r => {
      w(assertion(r.get))
      w.dismiss()
    })

    w.await(timeout(Span(5000, Millis)), dismissals(1))
  }

}

object OrganisaatioResults {
  def hae(implicit ec: ExecutionContext) = {
    Await.result(Future { Thread.sleep(50) }, Duration(1, TimeUnit.SECONDS))
    scala.io.Source.fromURL(getClass.getResource("/mock-data/organisaatio/organisaatio-hae.json")).mkString
  }
  val pikkola = scala.io.Source.fromURL(getClass.getResource("/mock-data/organisaatio/organisaatio-pikkola.json")).mkString
  def ysiysiysiysiysi(implicit ec: ExecutionContext) = {
    Await.result(Future { Thread.sleep(10) }, Duration(1, TimeUnit.SECONDS))
    scala.io.Source.fromURL(getClass.getResource("/mock-data/organisaatio/organisaatio-99999.json")).mkString
  }
}
