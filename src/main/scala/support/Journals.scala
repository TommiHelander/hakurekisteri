package support

import java.util.UUID

import akka.actor.ActorSystem
import fi.vm.sade.hakurekisteri.arvosana.{Arvosana, ArvosanaTable}
import fi.vm.sade.hakurekisteri.batchimport.{ImportBatch, ImportBatchTable}
import fi.vm.sade.hakurekisteri.opiskelija.{Opiskelija, OpiskelijaTable}
import fi.vm.sade.hakurekisteri.opiskeluoikeus.{Opiskeluoikeus, OpiskeluoikeusTable}
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriDriver.api._
import fi.vm.sade.hakurekisteri.rest.support.JDBCJournal
import fi.vm.sade.hakurekisteri.storage.repository.Journal
import fi.vm.sade.hakurekisteri.storage.HakurekisteriTables._
import fi.vm.sade.hakurekisteri.suoritus.{Suoritus, SuoritusTable}

import org.slf4j.LoggerFactory

trait Journals {
  val suoritusJournal: Journal[Suoritus, UUID]
  val opiskelijaJournal: Journal[Opiskelija, UUID]
  val opiskeluoikeusJournal: Journal[Opiskeluoikeus, UUID]
  val arvosanaJournal: JDBCJournal[Arvosana, UUID, ArvosanaTable]
  val eraJournal: Journal[ImportBatch, UUID]
}

class DbJournals(db: Database)(implicit val system: ActorSystem) extends Journals {
  lazy val log = LoggerFactory.getLogger(getClass)

  implicit val database = db

  override val suoritusJournal = new JDBCJournal[Suoritus, UUID, SuoritusTable](suoritusTable)
  override val opiskelijaJournal = new JDBCJournal[Opiskelija, UUID, OpiskelijaTable](opiskelijaTable)
  override val opiskeluoikeusJournal = new JDBCJournal[Opiskeluoikeus, UUID, OpiskeluoikeusTable](opiskeluoikeusTable)
  override val arvosanaJournal = new JDBCJournal[Arvosana, UUID, ArvosanaTable](arvosanaTable)
  override val eraJournal = new JDBCJournal[ImportBatch, UUID, ImportBatchTable](importBatchTable)

}
