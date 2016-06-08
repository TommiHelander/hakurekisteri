package fi.vm.sade.hakurekisteri.integration.virta

import org.joda.time.LocalDate

case class VirtaOpintosuoritus (
  suoritusPvm: LocalDate,
  nimi: Option[String],
  koulutuskoodi: Option[String],
  arvosana: Option[String],
  myontaja: String,
  laji: Option[String]
)