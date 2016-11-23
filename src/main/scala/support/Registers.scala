package support

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import fi.vm.sade.hakurekisteri.arvosana.{Arvosana, ArvosanaJDBCActor, ArvosanatQuery}
import fi.vm.sade.hakurekisteri.batchimport.{ImportBatch, ImportBatchActor}
import fi.vm.sade.hakurekisteri.integration.{ExecutorUtil, VirkailijaRestClient}
import fi.vm.sade.hakurekisteri.opiskelija.{Opiskelija, OpiskelijaJDBCActor}
import fi.vm.sade.hakurekisteri.opiskeluoikeus.{Opiskeluoikeus, OpiskeluoikeusJDBCActor}
import fi.vm.sade.hakurekisteri.organization.{FutureOrganizationHierarchy, OrganizationHierarchy}
import fi.vm.sade.hakurekisteri.rest.support.{Registers, Resource}
import fi.vm.sade.hakurekisteri.storage.Identified
import fi.vm.sade.hakurekisteri.suoritus._
import fi.vm.sade.hakurekisteri.{Config, KomoOids, Oids}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class BareRegisters(system: ActorSystem, journals: Journals) extends Registers {
  override val suoritusRekisteri = system.actorOf(Props(new SuoritusJDBCActor(journals.suoritusJournal, 5)), "suoritukset")
  override val ytlSuoritusRekisteri = system.actorOf(Props(new SuoritusJDBCActor(journals.suoritusJournal, 5)), "ytl-suoritukset")
  override val opiskelijaRekisteri = system.actorOf(Props(new OpiskelijaJDBCActor(journals.opiskelijaJournal, 5)), "opiskelijat")
  override val opiskeluoikeusRekisteri = system.actorOf(Props(new OpiskeluoikeusJDBCActor(journals.opiskeluoikeusJournal, 5)), "opiskeluoikeudet")
  override val arvosanaRekisteri = system.actorOf(Props(new ArvosanaJDBCActor(journals.arvosanaJournal, 5)), "arvosanat")
  override val ytlArvosanaRekisteri = system.actorOf(Props(new ArvosanaJDBCActor(journals.arvosanaJournal, 5)), "ytl-arvosanat")
  override val eraRekisteri: ActorRef = system.actorOf(Props(new ImportBatchActor(journals.eraJournal, 5)), "erat")
}

class AuthorizedRegisters(unauthorized: Registers, system: ActorSystem, config: Config) extends Registers {
  import akka.pattern.ask

  import scala.reflect.runtime.universe._
  implicit val ec:ExecutionContext = system.dispatcher

  val orgRestExecutor = ExecutorUtil.createExecutor(5, "authorizer-organization-rest-client-pool")
  val organisaatioClient: VirkailijaRestClient = new VirkailijaRestClient(config.integrations.organisaatioConfig, None)(orgRestExecutor, system)

  def authorizer[A <: Resource[I, A] : ClassTag: Manifest, I: Manifest](guarded: ActorRef, orgFinder: A => Option[String], komoFinder: A => Option[String]): ActorRef = {
    val resource = typeOf[A].typeSymbol.name.toString.toLowerCase
    system.actorOf(Props(new OrganizationHierarchy[A, I](guarded, (is: Seq[A]) => is.map(i => (i, orgFinder(i).map(Set(_)).getOrElse(Set()), komoFinder(i))), config, organisaatioClient)), s"$resource-authorizer")
  }

  def authorizer[A <: Resource[I, A] : ClassTag: Manifest, I: Manifest](guarded: ActorRef, orgFinder: A => Option[String]): ActorRef = {
    val resource = typeOf[A].typeSymbol.name.toString.toLowerCase
    system.actorOf(Props(new OrganizationHierarchy[A, I](guarded, (is: Seq[A]) => is.map(i => (i, orgFinder(i).map(Set(_)).getOrElse(Set()), None)), config, organisaatioClient)), s"$resource-authorizer")
  }

  def suoritusResolver(suoritukset: Seq[Suoritus]): Future[Seq[(Suoritus, Set[String], Option[String])]] = {
      val kielikoesuoritusIdt = suoritukset.collect {
        case s: VirallinenSuoritus with Identified[_] if s.komo == KomoOids.ammatillisenKielikoe =>
          s.id.asInstanceOf[UUID]
      }.toSet
      val kielikoesuoritustenMyontajat = if (kielikoesuoritusIdt.isEmpty) {
        Future.successful(Map.empty[UUID, Set[String]])
      } else {
        unauthorized.arvosanaRekisteri.?(ArvosanatQuery(kielikoesuoritusIdt))(Timeout(900, TimeUnit.SECONDS))
          .mapTo[Seq[Arvosana]].map(arvosanat => arvosanat.groupBy(_.suoritus).mapValues(_.map(_.source).toSet))
      }
      kielikoesuoritustenMyontajat.map(ksm => suoritukset.map {
        case s: VirallinenSuoritus with Identified[_] =>
          (s, ksm.getOrElse(s.id.asInstanceOf[UUID], Set(s.myontaja)), Some(s.komo))
        case s: VirallinenSuoritus => (s, Set(s.myontaja), Some(s.komo))
        case s => (s, Set.empty[String], None)
      })
    }

  def arvosanaResolver(arvosanat: Seq[Arvosana]): Future[Seq[(Arvosana, Set[String], Option[String])]] = {
      val arvosanatSuorituksittain = arvosanat.groupBy(_.suoritus)
      unauthorized.suoritusRekisteri.?(arvosanatSuorituksittain.keySet.toSeq)(Timeout(900, TimeUnit.SECONDS)).
        mapTo[Seq[Suoritus with Identified[UUID]]].map(suoritukset => {
        val suoritusAuthInfo = suoritukset.map(s => (s.id, s.asInstanceOf[Suoritus])).map {
          case (id, s: VirallinenSuoritus) if s.komo == KomoOids.ammatillisenKielikoe =>
            (id, arvosanatSuorituksittain(id).map(_.source).toSet)
          case (id, s: VirallinenSuoritus) => (id, Set(s.myontaja, s.source))
          case (id, s: VapaamuotoinenSuoritus) => (id, Set(s.source))
        }.toMap
        arvosanat.map(a => (a, suoritusAuthInfo(a.suoritus), None))
      })
    }

  override val suoritusRekisteri = system.actorOf(Props(new FutureOrganizationHierarchy[Suoritus, UUID](unauthorized.suoritusRekisteri, suoritusResolver, config, organisaatioClient)), "suoritus-authorizer")
  override val opiskelijaRekisteri = authorizer[Opiskelija, UUID](unauthorized.opiskelijaRekisteri, (opiskelija:Opiskelija) => Some(opiskelija.oppilaitosOid))
  override val opiskeluoikeusRekisteri = authorizer[Opiskeluoikeus, UUID](unauthorized.opiskeluoikeusRekisteri, (opiskeluoikeus:Opiskeluoikeus) => Some(opiskeluoikeus.myontaja), (opiskeluoikeus:Opiskeluoikeus) => Some(opiskeluoikeus.komo))
  override val arvosanaRekisteri = system.actorOf(Props(new FutureOrganizationHierarchy[Arvosana, UUID](unauthorized.arvosanaRekisteri, arvosanaResolver, config, organisaatioClient)), "arvosana-authorizer")
  override val eraRekisteri: ActorRef = authorizer[ImportBatch, UUID](unauthorized.eraRekisteri, (era:ImportBatch) => Some(Oids.ophOrganisaatioOid))
  override val ytlSuoritusRekisteri: ActorRef = null
  override val ytlArvosanaRekisteri: ActorRef = null
}
