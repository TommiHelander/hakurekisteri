package fi.vm.sade.hakurekisteri.web.arvosana

import _root_.akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem}
import fi.vm.sade.hakurekisteri.KomoOids
import fi.vm.sade.hakurekisteri.arvosana.{Arvosana, ArvosanaQuery}
import fi.vm.sade.hakurekisteri.integration.henkilo.IOppijaNumeroRekisteri
import fi.vm.sade.hakurekisteri.rest.support.User
import fi.vm.sade.hakurekisteri.suoritus.{Suoritus, VirallinenSuoritus}
import fi.vm.sade.hakurekisteri.web.rest.support.{HakurekisteriCrudCommands, HakurekisteriResource, Security, SecuritySupport}
import org.scalatra.swagger.Swagger

import scala.concurrent.Future

class ArvosanaResource(arvosanaActor: ActorRef, suoritusActor: ActorRef, oppijaNumeroRekisteri: IOppijaNumeroRekisteri)
                      (implicit sw: Swagger, s: Security, system: ActorSystem)
  extends HakurekisteriResource[Arvosana, CreateArvosanaCommand](arvosanaActor, ArvosanaQuery(_), oppijaNumeroRekisteri)
    with ArvosanaSwaggerApi
    with HakurekisteriCrudCommands[Arvosana, CreateArvosanaCommand]
    with SecuritySupport {

  override def createEnabled(resource: Arvosana, user: Option[User]): Future[Boolean] = updateEnabled(resource, user)

  override def updateEnabled(resource: Arvosana, user: Option[User]): Future[Boolean] =
    (suoritusActor ? resource.suoritus).mapTo[Option[Suoritus]].flatMap {
      case Some(v: VirallinenSuoritus) if v.komo == KomoOids.ammatillisenKielikoe => Future.successful(user.exists(_.isAdmin))
      case Some(v) => Future.successful(user.exists(_.username == v.source))
      case _ => super.updateEnabled(resource, user)
    }
}
