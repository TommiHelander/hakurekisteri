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
import scala.reflect.ClassTag
import slick.ast.{BaseTypedType, TypedType}
import slick.driver.PostgresDriver
import slick.jdbc.JdbcType
import slick.jdbc.meta.MTable
import slick.lifted
import slick.lifted.{PrimaryKey, ProvenShape, ShapedValue}
import slick.profile.RelationalProfile

import scala.concurrent.{Await, ExecutionContext}
import scala.xml.Elem


object HakurekisteriDriver extends PostgresDriver {

  override val columnTypes = new JdbcTypes {
    class UUIDJdbcType(implicit override val classTag: ClassTag[UUID]) extends super.UUIDJdbcType {
      override def sqlType = java.sql.Types.VARCHAR
      override def setValue(v: UUID, p: PreparedStatement, idx: Int) = p.setString(idx, v.toString)
      override def getValue(r: ResultSet, idx: Int) = UUID.fromString(r.getString(idx))
      override def updateValue(v: UUID, r: ResultSet, idx: Int) = r.updateString(idx, v.toString)
      override def valueToSQLLiteral(value: UUID) = if (value eq null) {
        "NULL"
      } else {
        val serialized = value.toString
        val sb = new StringBuilder
        sb append '\''
        for (c <- serialized) c match {
          case '\'' => sb append "''"
          case _ => sb append c
        }
        sb append '\''
        sb.toString()
      }
    }

    override val uuidJdbcType: super.UUIDJdbcType = new UUIDJdbcType
  }

  trait CustomImplicits extends super[PostgresDriver].Implicits with HakurekisteriJsonSupport {
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
                                                                (implicit val idType: TypedType[I])
  extends Table[Delta[R, I]](tag, name) with HakurekisteriColumns {

  def resourceId: Rep[I] = column[I]("resource_id")
  def source: Rep[String] = column[String]("source")
  def inserted: Rep[Long] = column[Long]("inserted")
  def deleted: Rep[Boolean] = column[Boolean]("deleted")

  val journalEntryShape = anyToToShapedValue((resourceId, inserted, deleted)).shaped

  def resourceShape: ShapedValue[_, ResourceRow]

  val combinedShape = journalEntryShape zip resourceShape

  def row(resource: R): Option[ResourceRow]

  def updateRow(r: R with Identified[I])(resourceData: ResourceRow): ((I, Long, Boolean), ResourceRow) =
    ((r.id, Platform.currentTime, false), resourceData)

  val deletedValues: (String) => ResourceRow

  def rowShaper(d: Delta[R, I]): Option[((I, Long, Boolean), ResourceRow)] = d match {
    case Deleted(id, source) => Some((id, Platform.currentTime, true), deletedValues(source))
    case Updated(r) => row(r).map(updateRow(r))
    case Insert(r) => throw new NotImplementedError("Insert deltas not implemented in JDBCJournal")
  }

  val resource: ResourceRow => R

  val extractSource: ResourceRow => String

  def delta(resourceId: I, inserted: Long, deleted: Boolean)(resourceData: ResourceRow): Delta[R, I] = if (deleted) {
    Deleted(resourceId, extractSource(resourceData))
  } else {
    val resource1 = resource(resourceData)
    Updated(resource1.identify(resourceId))
  }

  def deltaShaper(j: (I, Long, Boolean), rd: ResourceRow): Delta[R, I] = (delta _).tupled(j)(rd)

  def * : ProvenShape[Delta[R, I]] = ProvenShape.proveShapeOf(anyToToShapedValue(combinedShape) <> ((deltaShaper _).tupled, rowShaper))

  def pk: PrimaryKey = primaryKey(s"pk_$name", (resourceId, inserted))

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

  val resourcesWithVersions = table.groupBy[Rep[I], I, Rep[I], T](_.resourceId)

  val latest = for {
    (id: Rep[I], resource: lifted.Query[T, T#TableElementType, Seq]) <- resourcesWithVersions
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


