package fi.vm.sade.hakurekisteri.rest.support

import java.util.UUID

import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriDriver.api._
import fi.vm.sade.hakurekisteri.storage.Identified
import fi.vm.sade.hakurekisteri.storage.repository.{Deleted, Delta, Insert, Updated}
import slick.ast.BaseTypedType
import slick.lifted.{ProvenShape, ShapedValue}

import scala.compat.Platform
import scala.language.{existentials, postfixOps}

abstract class JournalTable[R <: Resource[I, R], I, ResourceRow](tag: Tag, name: String)
                                                                (implicit val idType: BaseTypedType[I])
  extends Table[Delta[R, I]](tag, name) with HakurekisteriColumns {

  def resourceId = column[I]("resource_id")
  def source = column[String]("source")
  def inserted = column[Long]("inserted")
  def deleted = column[Boolean]("deleted")
  def pk = primaryKey(s"pk_$name", (resourceId, inserted))

  type ShapedJournalRow = (Rep[I], Rep[String], Rep[Long], Rep[Boolean])
  type JournalRow = (UUID, Long, Boolean)

  val resource: ResourceRow => R
  val extractSource: ResourceRow => String

  def delta(resourceId: I, inserted: Long, deleted: Boolean)(resourceData: ResourceRow): Delta[R, I] = if (deleted) {
    Deleted(resourceId, extractSource(resourceData))
  } else {
    Updated(resource(resourceData).identify(resourceId))
  }

  def deltaShaper(j: (I, Long, Boolean), rd: ResourceRow): Delta[R, I] = (delta _).tupled(j)(rd)

  val deletedValues: (String) => ResourceRow

  def rowShaper(d: Delta[R, I]) = d match {
    case Deleted(id, source) => Some((id, Platform.currentTime, true), deletedValues(source))
    case Updated(r) => row(r).map(updateRow(r))
    case Insert(r) => throw new NotImplementedError("Insert deltas not implemented in JDBCJournal")
  }

  def updateRow(r: R with Identified[I])(resourceData: ResourceRow) = ((r.id, Platform.currentTime, false), resourceData)

  def row(resource: R): Option[ResourceRow]

  def resourceShape: ShapedValue[_, ResourceRow]

  private def combinedShape = (resourceId, inserted, deleted).shaped zip resourceShape

  def * : ProvenShape[Delta[R, I]] = {
    combinedShape <>((deltaShaper _).tupled, rowShaper)
  }
}
