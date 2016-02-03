package fi.vm.sade.hakurekisteri.integration.valintarekisteri

import java.net.URLEncoder

import akka.actor.Actor
import akka.pattern.pipe
import fi.vm.sade.hakurekisteri.Config
import fi.vm.sade.hakurekisteri.integration.VirkailijaRestClient
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}


class ValintarekisteriActor(restClient: VirkailijaRestClient, config: Config) extends Actor {

  implicit val ec: ExecutionContext = context.dispatcher

  private val ok = 200

  override def receive: Receive = {
    case ValintarekisteriQuery(henkiloOids, koulutuksenAlkamiskausi) =>
      fetchEnsimmainenVastaanotto(henkiloOids, koulutuksenAlkamiskausi) pipeTo sender
  }

  def fetchEnsimmainenVastaanotto(henkiloOids: Set[String], koulutuksenAlkamiskausi: String): Future[Seq[EnsimmainenVastaanotto]] = {
    val url = s"/ensikertalaisuus?koulutuksenAlkamiskausi=${URLEncoder.encode(koulutuksenAlkamiskausi, "UTF-8")}"
    restClient.postObject[Set[String], Seq[EnsimmainenVastaanotto]](url, ok, henkiloOids)
  }
}

case class ValintarekisteriQuery(henkiloOids: Set[String], koulutuksenAlkamiskausi: String)

case class EnsimmainenVastaanotto(oid: String, paattyi: Option[DateTime])
