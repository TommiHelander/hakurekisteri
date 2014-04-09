package fi.vm.sade.hakurekisteri

import org.scalatest.WordSpec
import fi.vm.sade.hakurekisteri.opiskelija.Opiskelija

import org.json4s.FieldSerializer._
import org.json4s.{CustomSerializer, FieldSerializer, DefaultFormats, Formats}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import java.util.{UUID, Date}
import org.scalatest.matchers.ShouldMatchers
import org.json4s.JsonAST.{JValue, JField, JString, JObject}
import fi.vm.sade.hakurekisteri.rest.support.{HakurekisteriJsonSupport, IdentitySerializer, UUIDSerializer}
import fi.vm.sade.hakurekisteri.storage.Identified
import fi.vm.sade.hakurekisteri.suoritus.{Komoto, Suoritus, Peruskoulu}
import java.text.SimpleDateFormat
import fi.vm.sade.hakurekisteri.suoritus.yksilollistaminen._
import fi.vm.sade.hakurekisteri.suoritus.Komoto
import fi.vm.sade.hakurekisteri.suoritus.Suoritus
import scala.Some
import org.joda.time.{MonthDay, DateTime}

class IdentifiableSerializationSpec extends WordSpec with ShouldMatchers with HakurekisteriJsonSupport {

  val identifier = UUID.randomUUID()
  val opiskelija = new Opiskelija("1.2.3", "9": String, "9A": String, "2.3.4": String, DateTime.now, Some(DateTime.now))

  val df = new SimpleDateFormat("yyyyMMdd")

  val kevatJuhla = new MonthDay(6,4).toLocalDate(DateTime.now.getYear).toDateTimeAtStartOfDay


  val suoritus = Peruskoulu("1.2.3", "KESKEN", kevatJuhla.toLocalDate, "1.2.4")



  "An identified suoritus " when {
    val s = Suoritus.identify(suoritus)
    "serialized" should {
      val result = serializeDeserialize[Suoritus, Suoritus](s)
      val identity = serializeDeserialize[Identified, Suoritus](s)
      "retain henkiloOid" in {
        result.henkiloOid should equal (s.henkiloOid)
      }

      "retain identity" in {
        identity.id should equal (s.id)
      }

    }
  }



  "An identified opiskelija " when {
    val o = Opiskelija.identify(opiskelija, identifier)
    "serialized" should {
      val result = serializeDeserialize[Opiskelija, Opiskelija](o)
      val identity = serializeDeserialize[Identified, Opiskelija](o)
      "retain henkiloOid" in {
        result.henkiloOid should equal (o.henkiloOid)
      }

      "retain luokkataso" in {
        result.luokkataso should equal (o.luokkataso)
      }

      "retain luokka" in {
        result.luokka should equal (o.luokka)
      }

      "retain loppuPaiva with 1s precision" in {
        result.loppuPaiva.get.toDate.getTime / 1000 should be (o.loppuPaiva.get.toDate.getTime / 1000)
      }

      "retain alkuPaiva with 1s precision" in {
        result.alkuPaiva.toDate.getTime / 1000 should be (o.alkuPaiva.toDate.getTime / 1000)
      }

      "retain oppilaitosOid" in {
        result.oppilaitosOid should equal (o.oppilaitosOid)
      }

      "retain identity" in {
        identity.id should equal (o.id)
      }


    }
  }


  def serializeDeserialize[A: Manifest, B: Manifest](o: B with Identified)  = {
    val json = write(o)
    read[A](json)
  }
}
