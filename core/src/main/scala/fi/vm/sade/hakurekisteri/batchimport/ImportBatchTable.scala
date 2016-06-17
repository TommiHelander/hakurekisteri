package fi.vm.sade.hakurekisteri.batchimport

import fi.vm.sade.hakurekisteri.rest.support.JournalTable
import java.util.UUID

import slick.driver.PostgresDriver.api._

import scala.xml.Elem
import BatchState.BatchState
import slick.lifted.{Index, ShapedValue}

object ImportBatchTable {
  type ImportBatchRow = (Elem, Option[String], String, String, BatchState, ImportStatus)
}

import ImportBatchTable._

import ImportBatchImplicits._

class ImportBatchTable(tag: Tag) extends JournalTable[ImportBatch, UUID, ImportBatchRow](tag, "import_batch") {
  def data: Rep[Elem] = column[Elem]("data")
  def externalId: Rep[Option[String]] = column[Option[String]]("external_id")
  def batchType: Rep[String] = column[String]("batch_type")
  def state: Rep[BatchState] = column[BatchState]("state")
  def status: Rep[ImportStatus] = column[ImportStatus]("status")

  def eIndex: Index = index("i_import_batch_external_id", externalId)
  def bIndex: Index = index("i_import_batch_batch_type", batchType)
  def sIndex: Index = index("i_import_batch_state", state)

  override def resourceShape: ShapedValue[(Rep[Elem], Rep[Option[String]], Rep[String], Any, Rep[BatchState], Rep[ImportStatus]), Nothing] =
    (data, externalId, batchType, source, state, status).shaped
  override def row(resource: ImportBatch): Option[ImportBatchTable.ImportBatchRow] = ImportBatch.unapply(resource)
  override val deletedValues: (String) => ImportBatchTable.ImportBatchRow = (lahde) => (<emptybatch/>, None, "deleted", lahde, BatchState.READY, ImportStatus())
  override val resource: (ImportBatchTable.ImportBatchRow) => ImportBatch = (ImportBatch.apply _).tupled
  override val extractSource: (ImportBatchTable.ImportBatchRow) => String = _._4
}
