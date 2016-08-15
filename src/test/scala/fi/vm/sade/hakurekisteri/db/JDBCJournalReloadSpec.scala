package fi.vm.sade.hakurekisteri.db

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriDriver.api._
import fi.vm.sade.hakurekisteri.rest.support.JDBCJournal
import fi.vm.sade.hakurekisteri.suoritus._
import fi.vm.sade.hakurekisteri.tools.ItPostgres
import fi.vm.sade.utils.tcp.ChooseFreePort
import org.joda.time.LocalDate
import org.scalatra.test.scalatest.ScalatraFunSuite
import org.slf4j.LoggerFactory

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}


class JDBCJournalReloadSpec extends ScalatraFunSuite {
  val logger = LoggerFactory.getLogger(getClass)
  val portChooser = new ChooseFreePort()
  val itDb = new ItPostgres(portChooser)
  itDb.start()
  implicit val database = Database.forURL(s"jdbc:postgresql://localhost:${portChooser.chosenPort}/suoritusrekisteri")

  override def stop(): Unit = {
    database.close()
    itDb.stop()
    super.stop()
  }

  def createSystemAndInsertAndShutdown(henkilot: Stream[UUID]) = {
    implicit val system = ActorSystem("test-jdbc")
    implicit val ec: ExecutionContext = system.dispatcher

    val suoritusJournal = new JDBCJournal[Suoritus, UUID, SuoritusTable](TableQuery[SuoritusTable])
    val suoritusrekisteri = system.actorOf(Props(new SuoritusJDBCActor(suoritusJournal, 1)))

    implicit val timeout: Timeout = 30.seconds
    val now = new LocalDate()

    val yoSuoritukset: Stream[Future[VirallinenSuoritus]] = henkilot.map((henkilo: UUID) => {
      (suoritusrekisteri ? VirallinenSuoritus(
        komo = "1.2.246.562.5.2013061010184237348007",
        myontaja = "1.2.246.562.10.43628088406",
        henkilo = henkilo.toString,
        yksilollistaminen = yksilollistaminen.Ei,
        suoritusKieli = "FI",
        lahde = "1.2.246.562.10.43628088406",
        tila = "VALMIS",
        valmistuminen = now
      )).mapTo[VirallinenSuoritus]
    })

    Await.result(Future.sequence(yoSuoritukset), Duration(30, TimeUnit.SECONDS))

    val suoritusFuture = (suoritusrekisteri ? SuoritusQuery()).mapTo[Seq[Suoritus]]

    val suoritukset = Await.result(suoritusFuture, Duration(30, TimeUnit.SECONDS))

    Await.result(system.terminate(), 15.seconds)
    //database.close()
    suoritukset
  }

  test("suoritukset should be deduplicated after reloading the journal") {
    val amount = 5
    val henkilot = Stream.continually(java.util.UUID.randomUUID).take(amount)

    createSystemAndInsertAndShutdown(henkilot)
    val suoritukset = createSystemAndInsertAndShutdown(henkilot)

    suoritukset.size should be (henkilot.length)
  }

}
