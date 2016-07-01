package support

import java.util.UUID

import akka.actor.ActorSystem
import fi.vm.sade.hakurekisteri.Config
import fi.vm.sade.hakurekisteri.arvosana.{Arvosana, ArvosanaTable}
import fi.vm.sade.hakurekisteri.batchimport.{ImportBatch, ImportBatchTable}
import fi.vm.sade.hakurekisteri.opiskelija.{Opiskelija, OpiskelijaTable}
import fi.vm.sade.hakurekisteri.opiskeluoikeus.{Opiskeluoikeus, OpiskeluoikeusTable}
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriDriver.simple._
import fi.vm.sade.hakurekisteri.rest.support.JDBCJournal
import fi.vm.sade.hakurekisteri.storage.repository.Journal
import fi.vm.sade.hakurekisteri.suoritus.{Suoritus, SuoritusTable}

trait Journals {
  val suoritusJournal: Journal[Suoritus, UUID]
  val opiskelijaJournal: Journal[Opiskelija, UUID]
  val opiskeluoikeusJournal: Journal[Opiskeluoikeus, UUID]
  val arvosanaJournal: Journal[Arvosana, UUID]
  val eraJournal: Journal[ImportBatch, UUID]
}

class DbJournals(config: Config)(implicit val system: ActorSystem) extends Journals {
  import fi.vm.sade.hakurekisteri.storage.HakurekisteriTables._
  implicit val database = config.database

  override val suoritusJournal = new JDBCJournal[Suoritus, UUID, SuoritusTable](suoritusTable)
  override val opiskelijaJournal = new JDBCJournal[Opiskelija, UUID, OpiskelijaTable](opiskelijaTable)
  override val opiskeluoikeusJournal = new JDBCJournal[Opiskeluoikeus, UUID, OpiskeluoikeusTable](opiskeluoikeusTable)
  override val arvosanaJournal = new JDBCJournal[Arvosana, UUID, ArvosanaTable](arvosanaTable)
  override val eraJournal = new JDBCJournal[ImportBatch, UUID, ImportBatchTable](importBatchTable)

}
