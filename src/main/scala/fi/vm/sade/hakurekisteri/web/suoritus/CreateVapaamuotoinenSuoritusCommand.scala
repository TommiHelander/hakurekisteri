package fi.vm.sade.hakurekisteri.web.suoritus

import java.util.Locale

import fi.vm.sade.hakurekisteri.suoritus.{Suoritus, VapaamuotoinenSuoritus}
import fi.vm.sade.hakurekisteri.web.rest.support.{HakurekisteriCommand, LocalDateSupport}
import org.scalatra.commands._


class CreateVapaamuotoinenSuoritusCommand extends HakurekisteriCommand[Suoritus] with LocalDateSupport {
  val kuvaus: Field[String] = asType[String]("kuvaus").notBlank
  val myontaja: Field[String] = asType[String]("myontaja").notBlank
  val tyyppi: Field[String] = asType[String]("tyyppi").notBlank
  val henkiloOid: Field[String] = asType[String]("henkiloOid").notBlank
  val vuosi: Field[Option[Int]] = asType[Option[Int]]("vuosi")
  val index: Field[Option[Int]] = asType[Option[Int]]("index")
  val lahde: Field[String] = asType[String]("lahde").notBlank
  val languages = Seq(Locale.getISOLanguages: _*) ++ Seq(Locale.getISOLanguages: _*).map(_.toUpperCase)

  override def toResource(user: String): Suoritus = VapaamuotoinenSuoritus(
    henkiloOid.value.get,
    kuvaus.value.get,
    myontaja.value.get,
    vuosi.value.get.get,
    tyyppi.value.get,
    index.value.get.getOrElse(0),
    lahde.value.get)
}

