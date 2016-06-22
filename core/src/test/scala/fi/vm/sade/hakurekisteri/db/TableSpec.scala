package fi.vm.sade.hakurekisteri.db

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable
import fi.vm.sade.hakurekisteri.batchimport.{BatchState, ImportBatch, ImportBatchTable, ImportStatus}
import fi.vm.sade.hakurekisteri.storage.repository.Updated
import java.util.UUID

import scala.concurrent.Await
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.existentials

class TableSpec extends FlatSpec with Matchers {

  behavior of "ImportBatchTable"

  val table = TableQuery[ImportBatchTable]

  it should "be able create itself" in {
    val db = getDb

    val tables = Await.result(db.run(
      table.schema.create.flatMap((u) => {
        MTable.getTables(table.baseTableRow.tableName)
      })
    ), 10.seconds)

    db.close()

    tables.size should be(1)
  }

  def getDb: Database = {
    Database.forURL("jdbc:h2:mem:test", driver = "org.h2.Driver")
  }

  it should "be able to store updates" in {
    val db = getDb
    val xml = <batchdata>data</batchdata>
    val batch = ImportBatch(xml, Some("externalId"), "test", "test", BatchState.READY, ImportStatus()).identify(UUID.randomUUID())

    val result = Await.result(db.run(
        table += Updated(batch)
    ), 10.seconds)

    db.close()

    result should be(1)
  }

  it should "be able to retrieve updates" in {
    val db = getDb
    val xml = <batchdata>data</batchdata>
    val batch = ImportBatch(xml, Some("externalId"), "test", "test", BatchState.READY, ImportStatus(new DateTime(), Some(new DateTime()), Map("foo" -> Set("foo exception")), Some(1), Some(0), Some(1))).identify(UUID.randomUUID())
    val table = TableQuery[ImportBatchTable]

    val q = table.filter(_.resourceId === batch.id)

    val result = Await.result(db.run(
      DBIO.seq(
        table += Updated(batch),
        q.result
      )
    ), 10.seconds)

    db.close()

    result should be(batch)
  }

}
