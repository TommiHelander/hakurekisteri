package fi.vm.sade.hakurekisteri.web.suoritus

import _root_.akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem}
import fi.vm.sade.hakurekisteri.KomoOids
import fi.vm.sade.hakurekisteri.integration.parametrit.{IsRestrictionActive, ParameterActor}
import fi.vm.sade.hakurekisteri.rest.support.Query
import fi.vm.sade.hakurekisteri.suoritus.{SuoritusQuery, Suoritus, VirallinenSuoritus}
import fi.vm.sade.hakurekisteri.web.rest.support._
import org.scalatra.swagger.Swagger

import scala.concurrent.Future

class SuoritusResource
      (suoritusRekisteriActor: ActorRef, parameterActor: ActorRef, queryMapper: (Map[String, String]) => Query[Suoritus])
      (implicit sw: Swagger, s: Security, system: ActorSystem)
  extends HakurekisteriResource[Suoritus, CreateSuoritusCommand](suoritusRekisteriActor, SuoritusQuery(_)) with SuoritusSwaggerApi with HakurekisteriCrudCommands[Suoritus, CreateSuoritusCommand] {

  override def createEnabled(resource: Suoritus) = {
    val curUser = currentUser.get
    resource match {
      case s: VirallinenSuoritus =>
        if (KomoOids.toisenAsteenVirkailijanKoulutukset.contains(s.komo) && !curUser.isAdmin) {
          (parameterActor ? IsRestrictionActive(ParameterActor.opoUpdateGraduation))
            .mapTo[Boolean]
            .map(x => !x)
        }
        else Future.successful(true)
      case _ => Future.successful(true)
    }
  }

  override def updateEnabled(resource: Suoritus) = createEnabled(resource)
}