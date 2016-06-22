package fi.vm.sade.hakurekisteri.rest.support

import java.sql.{PreparedStatement, ResultSet}
import java.util.UUID

import akka.actor.ActorSystem
import akka.event.Logging
import fi.vm.sade.hakurekisteri.storage.Identified
import fi.vm.sade.hakurekisteri.storage.repository._
import fi.vm.sade.hakurekisteri.tools.SafeXML
import org.json4s.JsonAST.JValue

import scala.compat.Platform
import scala.language.existentials
import scala.language.postfixOps
import scala.reflect.ClassTag
import slick.ast.{BaseTypedType, TypedType}
import slick.driver.PostgresDriver
import slick.jdbc.JdbcType
import slick.jdbc.meta.MTable
import slick.lifted.{ProvenShape, ShapedValue}
import slick.profile.RelationalProfile

import scala.concurrent.{Await, ExecutionContext}
import scala.xml.Elem


object HakurekisteriDriver extends PostgresDriver {

  trait CustomImplicits extends super.Implicits with HakurekisteriJsonSupport {
    class JValueType(implicit tmd: JdbcType[String], override val classTag: ClassTag[JValue])
      extends HakurekisteriDriver.MappedJdbcType[JValue, String] with BaseTypedType[JValue] {

      import org.json4s.jackson.JsonMethods._
      override def newSqlType: Option[Int] = Option(java.sql.Types.CLOB)
      override def newSqlTypeName(name: Option[RelationalProfile.ColumnOption.Length]): Option[String] = Some("TEXT")
      override def comap(json: String): JValue = parse(json)
      override def map(data: JValue): String = compact(render(data))
    }

    implicit val jvalueType = new JValueType

    class ElemType(implicit tmd: JdbcType[String], override val classTag: ClassTag[Elem])
      extends HakurekisteriDriver.MappedJdbcType[Elem, String] with BaseTypedType[Elem] {

      override def newSqlType: Option[Int] = Option(java.sql.Types.CLOB)
      override def newSqlTypeName(name: Option[RelationalProfile.ColumnOption.Length]): Option[String] = Some("TEXT")
      override def comap(xml: String): Elem = SafeXML.loadString(xml)
      override def map(data: Elem): String = data.toString()
    }

    implicit val elemType = new ElemType

  }

  override val api = new API with CustomImplicits {}
}

import HakurekisteriDriver.api._

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
    val resource1 = resource(resourceData)
    Updated(resource1.identify(resourceId))
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

import scala.concurrent.duration._

class JDBCJournal[R <: Resource[I, R], I, T <: JournalTable[R, I, _]](val table: TableQuery[T])
                                                                     (implicit val db: Database, val idType: BaseTypedType[I], implicit val system: ActorSystem)
  extends Journal[R, I] {

  implicit val ec: ExecutionContext = system.dispatcher
  val log = Logging.getLogger(system, this)
  lazy val tableName = table.baseTableRow.tableName

  Await.result(db.run(MTable.getTables(tableName).flatMap((t: Vector[MTable]) => {
    if (t.isEmpty) {
      DBIO.seq(schemaActionExtensionMethods(tableQueryToTableQueryExtensionMethods(table).schema).create)
    } else {
      DBIO.seq()
    }
  })), 30.seconds)

  log.debug(s"started ${getClass.getSimpleName} with table $tableName")

  override def addModification(o: Delta[R, I]): Unit = ???
    //TODO
    /*
    db withSession (implicit session =>
      table += o
    )
    */

  override def journal(latestQuery: Option[Long]): Seq[Delta[R, I]] = latestQuery match {
    case None =>
      Await.result(db.run(latestResources.result), 2.minutes)
    case Some(lat) =>
      Await.result(db.run(latestResources.filter(_.inserted >= lat).result), 2.minutes)
  }

  import slick.lifted

  val resourcesWithVersions = table.groupBy[lifted.Rep[I], I, lifted.Rep[I], T](_.resourceId)

  val latest = for {
    (id: lifted.Rep[I], resource: lifted.Query[T, T#TableElementType, Seq]) <- resourcesWithVersions
  } yield (id, resource.map(_.inserted).max)

  val result: lifted.Query[T, Delta[R, I], Seq] = for (
    delta <- table;
    (id, timestamp) <- latest
    if delta.resourceId === id && delta.inserted === timestamp

  ) yield delta

  val latestResources = {
    result.sortBy(_.inserted.asc)
  }
}


