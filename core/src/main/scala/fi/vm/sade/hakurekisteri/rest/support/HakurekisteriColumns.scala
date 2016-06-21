package fi.vm.sade.hakurekisteri.rest.support

import java.util.UUID

import org.joda.time.{DateTime, LocalDate}
import fi.vm.sade.hakurekisteri.suoritus.yksilollistaminen.Yksilollistetty
import fi.vm.sade.hakurekisteri.suoritus.yksilollistaminen
import org.json4s.Extraction
import org.json4s.JsonDSL._
import HakurekisteriDriver.api._
import fi.vm.sade.hakurekisteri.batchimport.{BatchState, ImportStatus}
import fi.vm.sade.hakurekisteri.batchimport.BatchState._
import org.json4s.JsonAST.JValue


trait HakurekisteriColumns extends HakurekisteriJsonSupport {
  implicit def datetimeLong = MappedColumnType.base[DateTime, Long](
    _.getMillis,
    new DateTime(_)
  )

  implicit def localDateString = MappedColumnType.base[LocalDate, String](
    _.toString,
    LocalDate.parse
  )

  implicit def yksilollistaminenString = MappedColumnType.base[Yksilollistetty, String](
    _.toString,
    yksilollistaminen.withName
  )

  implicit def jsonMap = MappedColumnType.base[Map[String, String], JValue](
    map2jvalue,
    _.extractOpt[Map[String, String]].getOrElse(Map())
  )

  implicit def batchStateColumnType = MappedColumnType.base[BatchState, String](
    _.toString,
    BatchState.withName
  )

  implicit def importstatusType = MappedColumnType.base[ImportStatus, JValue](
    Extraction.decompose,
    _.extract[ImportStatus]
  )

  implicit def uuidType = MappedColumnType.base[UUID, String](
    _.toString,
    UUID.fromString
  )

}

object HakurekisteriColumns extends HakurekisteriColumns
