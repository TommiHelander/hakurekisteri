package fi.vm.sade.hakurekisteri.web.suoritus

import fi.vm.sade.hakurekisteri.suoritus.{Suoritus, VapaamuotoinenSuoritus}
import fi.vm.sade.hakurekisteri.web.rest.support.HakurekisteriResource

trait VapaamuotoinenSuoritusSwaggerApi extends SuoritusSwaggerModel { this: HakurekisteriResource[Suoritus, CreateVapaamuotoinenSuoritusCommand] =>

  protected val applicationDescription = "Vapaamuotoisten suoritustietojen rajapinta"

  //registerModel(suoritusModel)
  registerModel(vapaamuotoinenSuoritusModel)

  val query = apiOperation[Seq[Suoritus]]("haeSuoritukset")
    .summary("näyttää kaikki suoritukset")
    .notes("Näyttää kaikki suoritukset. Voit myös hakea eri parametreillä.")
    .parameter(queryParam[Option[String]]("henkilo").description("henkilon oid"))
    .parameter(queryParam[Option[String]]("kausi").description("päättymisen kausi").allowableValues("S", "K"))
    .parameter(queryParam[Option[String]]("vuosi").description("päättymisen vuosi"))
    .parameter(queryParam[Option[String]]("myontaja").description("myöntäneen oppilaitoksen oid"))
    .parameter(queryParam[Option[String]]("komo").description("koulutusmoduulin oid"))
    .parameter(queryParam[Option[String]]("muokattuJalkeen").description("ISO aikaleima (esim. 2015-01-01T12:34:56.000+02:00) jonka jälkeen muokatut"))

  val create = apiOperation[VapaamuotoinenSuoritus]("lisääSuoritus")
    .summary("luo suorituksen ja palauttaa sen tiedot")
    .parameter(bodyParam[VapaamuotoinenSuoritus]("suoritus").description("uusi suoritus").required)

  val update =  apiOperation[VapaamuotoinenSuoritus]("päivitäSuoritus")
    .summary("päivittää olemassa olevaa suoritusta ja palauttaa sen tiedot")
    .parameter(pathParam[String]("id").description("suorituksen uuid").required)
    .parameter(bodyParam[VapaamuotoinenSuoritus]("suoritus").description("päivitettävä suoritus").required)

  val read = apiOperation[Suoritus]("haeSuoritus")
    .summary("hakee suorituksen tiedot")
    .parameter(pathParam[String]("id").description("suorituksen uuid").required)

  val delete = apiOperation[Unit]("poistaSuoritus")
    .summary("poistaa olemassa olevan suoritustiedon")
    .parameter(pathParam[String]("id").description("suoritustiedon uuid").required)

}


