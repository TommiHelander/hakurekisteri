package fi.vm.sade.hakurekisteri.integration.ytl

import java.util.Date

import fi.vm.sade.hakurekisteri.arvosana.{ArvioOsakoe, ArvioYo}
import fi.vm.sade.hakurekisteri.integration.ytl.YTLXml.YoTutkinto
import fi.vm.sade.hakurekisteri.suoritus.{VirallinenSuoritus}
import jawn._
import jawn.ast.{JValue, JParser}
import org.joda.time.{LocalDate}
import org.joda.time.format.DateTimeFormat
import org.json4s.jackson.JsonMethods._
import org.json4s.{CustomSerializer}
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.jackson.Serialization

import scala.util.{Try}
case class Operation(operationUuid: String)

trait Status {}

case class InProgress() extends Status
case class Finished() extends Status
case class Failed() extends Status

object Student {

  private val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")

  case object DateSerializer extends CustomSerializer[LocalDate](format => ( {
    case JString(s) => dtf.parseLocalDate(s)
  }, {
    case d: Date => throw new UnsupportedOperationException("Serialization is unsupported")
  }
    )
  )

  val formatsStudent = Serialization.formats(org.json4s.NoTypeHints) + KausiDeserializer + DateSerializer


  private def jvalueToStudent(jvalue: ast.JValue): Student = {
    implicit val formats = formatsStudent
  // this is slow
  parse(jvalue.render()).extract[Student]
}

  case class StudentAsyncParser() {
    val p: AsyncParser[JValue] = JParser.async(mode = AsyncParser.UnwrapArray)

    def feedChunk(c: Array[Byte]): Seq[Try[Student]] = {
      p.absorb(c) match {
        case Right(js: Seq[JValue]) => js.map(v => Try(jvalueToStudent(v)))
        case Left(e) => throw e
      }
    }

  }
}

case object KausiDeserializer extends CustomSerializer[fi.vm.sade.hakurekisteri.integration.ytl.Kausi](format => ({
  case JString(arvosana) =>
    fi.vm.sade.hakurekisteri.integration.ytl.Kausi(arvosana)
},
  {
    case x: fi.vm.sade.hakurekisteri.integration.ytl.Kausi =>
      throw new UnsupportedOperationException("Serialization is unsupported")
  }
  ))

case object StatusDeserializer extends CustomSerializer[Status](format => ({
  case JObject(fields) if fields.exists {
    case ("finished", value: JString) => true
    case _ => false
  } => Finished()
  case JObject(fields) if fields.exists {
    case ("failure", value: JString) => true
    case _ => false
  } => Failed()

  case JObject(e) => InProgress()
}, {
  case x: Status => throw new UnsupportedOperationException("Serialization is unsupported")
}))

case class Student(ssn: String, lastname: String, firstnames: String,
                   graduationPeriod: Option[Kausi] = None,
                   graduationDate: Option[LocalDate] = None,
                   graduationSchoolOphOid: Option[String] = None,
                   graduationSchoolYtlNumber: Option[String] = None,
                   language: Option[String] = None,
                   exams: Seq[Exam])

case class Exam(examId: String,examRole: String, period: Kausi, grade: String, points: Option[Int], sections: Seq[Section])

case class Section(sectionId: String, sectionPoints: String)

trait Kausi {
  def toLocalDate : LocalDate
}
case class Kevat(vuosi:Int) extends Kausi {
  override def toLocalDate = YTLXml.kevaanAlku.toLocalDate(vuosi.toInt)
}
case class Syksy(vuosi:Int) extends Kausi{
  override def toLocalDate = YTLXml.syys.toLocalDate(vuosi.toInt)
}

object Kausi {
  private val kausi = """(\d\d\d\d)(K|S)""".r

  def apply(kausiSpec: String): Kausi = kausiSpec match {
    case kausi(year, "K") => Kevat(Integer.parseInt(year))
    case kausi(year, "S") => Syksy(Integer.parseInt(year))
    case kausi(year, c) => throw new IllegalArgumentException(s"Illegal last character '$c'. Valid values are 'K' and 'S'")
    case s => throw new IllegalArgumentException(s"Illegal kausi specification '$s'. Expected format YYYY(K|S), e.g. 2015K")
  }
}

object StudentToKokelas {

  def convert(oid: String, s: Student): Kokelas = {
    val suoritus: VirallinenSuoritus = toYoTutkinto(oid, s)
    val yoKokeet = s.exams.map(exam => YoKoe(ArvioYo(exam.grade, exam.points), exam.examId, exam.examRole, exam.period.toLocalDate))
    val osakokeet = s.exams.flatMap(exam => exam.sections.map(section => {
      Osakoe(ArvioOsakoe(section.sectionPoints),exam.examId, section.sectionId, exam.examRole, exam.period.toLocalDate)
    }))
    Kokelas(oid,suoritus,None,yoKokeet,osakokeet)
  }

  def toYoTutkinto(oid: String, s: Student): VirallinenSuoritus = {
    val valmistuminen: LocalDate = s.graduationPeriod.map(_.toLocalDate).getOrElse(YTLXml.parseKausi(YTLXml.nextKausi).get)
    val valmis = s.graduationPeriod.isDefined
    val suoritus = YoTutkinto(suorittaja = oid, valmistuminen = valmistuminen, kieli = s.language.get, valmis = valmis)
    suoritus
  }
}