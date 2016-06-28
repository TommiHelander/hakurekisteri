package fi.vm.sade.hakurekisteri.rest.support

import slick.driver.PostgresDriver

object HakurekisteriDriver extends PostgresDriver {

  override val api = new API with HakurekisteriColumns {}

}