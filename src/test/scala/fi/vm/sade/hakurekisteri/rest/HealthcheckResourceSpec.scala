package fi.vm.sade.hakurekisteri.rest

import java.util.UUID

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import fi.vm.sade.hakurekisteri.Config
import fi.vm.sade.hakurekisteri.acceptance.tools.FakeAuthorizer
import fi.vm.sade.hakurekisteri.arvosana._
import fi.vm.sade.hakurekisteri.batchimport._
import fi.vm.sade.hakurekisteri.ensikertalainen.{QueriesRunning, QueryCount}
import fi.vm.sade.hakurekisteri.healthcheck.{HealthcheckActor, Status}
import fi.vm.sade.hakurekisteri.integration.hakemus.{FullHakemus, HakemusQuery}
import fi.vm.sade.hakurekisteri.integration.virta.{VirtaHealth, VirtaStatus}
import fi.vm.sade.hakurekisteri.integration.ytl.{Report, YtlReport}
import fi.vm.sade.hakurekisteri.opiskelija.{Opiskelija, OpiskelijaJDBCActor, OpiskelijaTable}
import fi.vm.sade.hakurekisteri.opiskeluoikeus.{Opiskeluoikeus, OpiskeluoikeusJDBCActor, OpiskeluoikeusTable}
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriDriver.api._
import fi.vm.sade.hakurekisteri.rest.support.JDBCJournal
import fi.vm.sade.hakurekisteri.storage.Identified
import fi.vm.sade.hakurekisteri.storage.repository.{InMemJournal, Updated}
import fi.vm.sade.hakurekisteri.suoritus._
import fi.vm.sade.hakurekisteri.tools.{ItPostgres, Peruskoulu}
import fi.vm.sade.hakurekisteri.web.healthcheck.HealthcheckResource
import fi.vm.sade.utils.tcp.ChooseFreePort
import org.joda.time.{DateTime, LocalDate}
import org.json4s.JsonAST.JInt
import org.scalatra.test.scalatest.ScalatraFunSuite

import scala.concurrent.{Await, ExecutionContext}


class HealthcheckResourceSpec extends ScalatraFunSuite {
  val config = Config.mockConfig
  val arvosana = Arvosana(UUID.randomUUID(), Arvio410("10"), "AI", None, false, source = "Test",lahdeArvot = Map())
  val opiskelija = Opiskelija("1.2.3", "9", "9A", "1.2.4", DateTime.now, None, source = "Test")
  val opiskeluoikeus = Opiskeluoikeus(LocalDate.now(), None, "1.2.4", "1.2.5", "1.2.3", source = "Test")
  val suoritus = Peruskoulu("1.2.3", "KESKEN", LocalDate.now,"1.2.4")
  val importBatch = ImportBatch(<empty/>, None, "test", "foo", BatchState.DONE, ImportStatus())
  val hakemus = FullHakemus("1.2.5", Some("1.2.4"), "1.2.5", None, state =  Some("ACTIVE"), Seq())

  implicit val system = ActorSystem()
  implicit val ec: ExecutionContext = system.dispatcher
  val portChooser = new ChooseFreePort
  val itDb = new ItPostgres(portChooser)
  itDb.start()
  implicit val database = Database.forURL(s"jdbc:postgresql://localhost:${portChooser.chosenPort}/suoritusrekisteri")

  val opiskeluoikeusJournal = new JDBCJournal[Opiskeluoikeus, UUID, OpiskeluoikeusTable](TableQuery[OpiskeluoikeusTable])
  opiskeluoikeusJournal.addModification(Updated(opiskeluoikeus.identify))
  val opiskeluoikeusRekisteri = system.actorOf(Props(new OpiskeluoikeusJDBCActor(opiskeluoikeusJournal, 1)))
  val guardedOpiskeluoikeusRekisteri = system.actorOf(Props(new FakeAuthorizer(opiskeluoikeusRekisteri)))

  val suoritusJournal = new JDBCJournal[Suoritus, UUID, SuoritusTable](TableQuery[SuoritusTable])
  suoritusJournal.addModification(Updated(suoritus.identify))
  val suoritusRekisteri = system.actorOf(Props(new SuoritusJDBCActor(suoritusJournal, 1)))
  val guardedSuoritusRekisteri = system.actorOf(Props(new FakeAuthorizer(suoritusRekisteri)))

  val arvosanaJournal = new JDBCJournal[Arvosana, UUID, ArvosanaTable](TableQuery[ArvosanaTable])
  arvosanaJournal.addModification(Updated(arvosana.identify))
  val arvosanaRekisteri = system.actorOf(Props(new ArvosanaJDBCActor(arvosanaJournal, 1)))
  val guardedArvosanaRekisteri = system.actorOf(Props(new FakeAuthorizer(arvosanaRekisteri)))

  val opiskelijaJournal = new JDBCJournal[Opiskelija, UUID, OpiskelijaTable](TableQuery[OpiskelijaTable])
  opiskelijaJournal.addModification(Updated(opiskelija.identify))
  val opiskelijaRekisteri = system.actorOf(Props(new OpiskelijaJDBCActor(opiskelijaJournal, 1)))
  val guardedOpiskelijaRekisteri = system.actorOf(Props(new FakeAuthorizer(opiskelijaRekisteri)))

  val eraJournal = new JDBCJournal[ImportBatch, UUID, ImportBatchTable](TableQuery[ImportBatchTable])
  val eraRekisteri = system.actorOf(Props(new ImportBatchActor(eraJournal, 1)))
  val guardedEraRekisteri = system.actorOf(Props(new FakeAuthorizer(eraRekisteri)))

  val hakemukset = system.actorOf(Props(new Actor {
    override def receive: Actor.Receive = {
      case h: HakemusQuery => val identify: FullHakemus with Identified[String] = hakemus.identify
        sender ! Seq(identify)
    }
  }))

  val ensikertalainen = system.actorOf(Props(new Actor {
    override def receive: Actor.Receive = {
      case QueryCount => sender ! QueriesRunning(Map("status" -> 1))
    }
  }))

  val ytl = system.actorOf(Props(new Actor {
    override def receive: Actor.Receive = {
      case Report => sender ! YtlReport(Seq(), None)
    }
  }))

  val virtaQueue = system.actorOf(Props(new Actor {
    override def receive: Receive = {
      case VirtaHealth => sender ! VirtaStatus(Some(new DateTime()), Some(false), 1000, Status.OK)
    }
  }))

  val healthcheck = system.actorOf(Props(new HealthcheckActor(guardedArvosanaRekisteri, guardedOpiskelijaRekisteri, guardedOpiskeluoikeusRekisteri, guardedSuoritusRekisteri, guardedEraRekisteri, ytl, ensikertalainen, virtaQueue, config)))

  addServlet(new HealthcheckResource(healthcheck), "/*")

  import org.json4s.jackson.JsonMethods._

  test("healthcheck should return OK and correct resource counts") {

    import scala.concurrent.duration._
    implicit val timeout: Timeout = 60.seconds
    val saved = (eraRekisteri ? importBatch).mapTo[ImportBatch with Identified[UUID]]
    Await.result(saved, 60.seconds)

    get("/") {
      status should equal (200)
      body should include ("\"status\":\"OK\"")
      parse(body) \\ "arvosanat" \ "count"   should equal(JInt(1))
      parse(body) \\ "opiskelijat" \ "count" should equal(JInt(1))
      parse(body) \\ "opiskeluoikeudet" \ "count" should equal(JInt(1))
      parse(body) \\ "suoritukset" \ "count" should equal(JInt(1))
      parse(body) \\ "erat" \ "count" should equal(JInt(1))
      response.getHeader("Expires") should not be null
    }
  }

  override def afterAll(): Unit = {
    import scala.concurrent.duration._
    Await.result(system.terminate(), 15.seconds)
    database.close()
    itDb.stop()
    super.afterAll()
  }

  def seq2journal[R <: fi.vm.sade.hakurekisteri.rest.support.Resource[UUID, R]](s:Seq[R]) = {
    val journal = new InMemJournal[R, UUID]
    s.foreach((resource:R) => journal.addModification(Updated(resource.identify(UUID.randomUUID()))))
    journal
  }

}
