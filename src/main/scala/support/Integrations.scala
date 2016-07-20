package support

import java.util.concurrent.TimeUnit

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import fi.vm.sade.hakurekisteri.Config
import fi.vm.sade.hakurekisteri.integration.hakemus._
import fi.vm.sade.hakurekisteri.integration.henkilo.MockHenkiloActor
import fi.vm.sade.hakurekisteri.integration.koodisto.KoodistoActor
import fi.vm.sade.hakurekisteri.integration.organisaatio.{HttpOrganisaatioActor, MockOrganisaatioActor}
import fi.vm.sade.hakurekisteri.integration.parametrit.{HttpParameterActor, MockParameterActor}
import fi.vm.sade.hakurekisteri.integration.valintarekisteri.{ValintarekisteriActor, ValintarekisteriQuery}
import fi.vm.sade.hakurekisteri.integration.{ExecutorUtil, VirkailijaRestClient}
import fi.vm.sade.hakurekisteri.integration._
import fi.vm.sade.hakurekisteri.integration.tarjonta.{MockTarjontaActor, TarjontaActor}
import fi.vm.sade.hakurekisteri.integration.valintatulos.ValintaTulosActor
import fi.vm.sade.hakurekisteri.integration.virta.{VirtaOpintosuoritus, _}
import fi.vm.sade.hakurekisteri.integration.ytl.YtlActor
import fi.vm.sade.hakurekisteri.rest.support.Registers
import fi.vm.sade.hakurekisteri.web.proxies.{HttpProxies, MockProxies, Proxies}
import org.joda.time.LocalDate

trait Integrations {
  val hakuAppPermissionChecker: ActorRef
  val virta: ActorRef
  val virtaResource: ActorRef
  val henkilo: ActorRef
  val organisaatiot: ActorRef
  val hakemukset: ActorRef
  val tarjonta: ActorRef
  val koodisto: ActorRef
  val ytl: ActorRef
  val parametrit: ActorRef
  val valintaTulos: ActorRef
  val valintarekisteri: ActorRef
  val proxies: Proxies
  val hakemusClient: VirkailijaRestClient
}

object Integrations {
  def apply(rekisterit: Registers,
            system: ActorSystem,
            config: Config): Integrations = config.mockMode match {
    case true => new MockIntegrations(rekisterit, system, config)
    case _ => new BaseIntegrations(rekisterit, system, config)
  }
}

class MockIntegrations(rekisterit: Registers, system: ActorSystem, config: Config) extends Integrations {
  override val virta: ActorRef = mockActor("virta", new DummyActor)
  override val virtaResource: ActorRef = mockActor("virtaResource", new MockVirtaResourceActor)
  override val valintaTulos: ActorRef = mockActor("valintaTulos", new DummyActor)
  override val valintarekisteri: ActorRef = mockActor("valintarekisteri", new Actor {
    override def receive: Receive = {
      case ValintarekisteriQuery(_, _) => sender ! Seq()
      case a => println(s"DummyActor($self): received $a")
    }
  })
  override val hakemukset: ActorRef = mockActor("hakemukset", new MockHakemusActor)
  override val koodisto: ActorRef = mockActor("koodisto", new DummyActor)
  override val organisaatiot: ActorRef = mockActor("organisaatiot", new MockOrganisaatioActor(config))
  override val parametrit: ActorRef = mockActor("parametrit", new MockParameterActor)
  override val henkilo: ActorRef = mockActor("henkilo", new MockHenkiloActor(config))
  override val tarjonta: ActorRef = mockActor("tarjonta", new MockTarjontaActor(config))
  override val ytl: ActorRef = system.actorOf(Props(new YtlActor(
    henkilo,
    rekisterit.suoritusRekisteri,
    rekisterit.arvosanaRekisteri,
    hakemukset,
    config.integrations.ytlConfig
  )), "ytl")
  override val proxies = new MockProxies
  override val hakemusClient = null

  private def mockActor(name: String, actor: => Actor) = system.actorOf(Props(actor), name)

  override val hakuAppPermissionChecker: ActorRef = system.actorOf(Props(new Actor {
    override def receive: Receive = {
      case a: HasPermission => sender ! true
    }
  }))
}


class BaseIntegrations(rekisterit: Registers,
                       system: ActorSystem,
                       config: Config) extends Integrations {
  val restEc = ExecutorUtil.createExecutor(10, "rest-client-pool")
  val vtsEc = ExecutorUtil.createExecutor(5, "valinta-tulos-client-pool")
  val vrEc = ExecutorUtil.createExecutor(10, "valintarekisteri-client-pool")
  val virtaEc = ExecutorUtil.createExecutor(1, "virta-client-pool")
  val virtaResourceEc = ExecutorUtil.createExecutor(5, "virta-resource-client-pool")

  system.registerOnTermination(() => {
    restEc.shutdown()
    vtsEc.shutdown()
    vrEc.shutdown()
    virtaEc.shutdown()
    restEc.awaitTermination(3, TimeUnit.SECONDS)
    vtsEc.awaitTermination(3, TimeUnit.SECONDS)
    vrEc.awaitTermination(3, TimeUnit.SECONDS)
    virtaEc.awaitTermination(3, TimeUnit.SECONDS)
    virtaResourceEc.awaitTermination(3, TimeUnit.SECONDS)
  })

  private val tarjontaClient = new VirkailijaRestClient(config.integrations.tarjontaConfig, None)(restEc, system)
  private val organisaatioClient = new VirkailijaRestClient(config.integrations.organisaatioConfig, None)(restEc, system)
  private val koodistoClient = new VirkailijaRestClient(config.integrations.koodistoConfig, None)(restEc, system)
  private val henkiloClient = new VirkailijaRestClient(config.integrations.henkiloConfig, None)(restEc, system)
  val hakemusClient = new VirkailijaRestClient(config.integrations.hakemusConfig.serviceConf, None)(restEc, system)
  private val parametritClient = new VirkailijaRestClient(config.integrations.parameterConfig, None)(restEc, system)
  private val valintatulosClient = new VirkailijaRestClient(config.integrations.valintaTulosConfig, None)(vtsEc, system)
  private val valintarekisteriClient = new VirkailijaRestClient(config.integrations.valintarekisteriConfig, None)(vrEc, system)
  private val hakuAppPermissionCheckerClient = new VirkailijaRestClient(config.integrations.hakemusConfig.serviceConf.copy(
    casUrl = None, user = None, password = None
  ), None)(restEc, system)

  val tarjonta = system.actorOf(Props(new TarjontaActor(tarjontaClient, config)), "tarjonta")
  val organisaatiot = system.actorOf(Props(new HttpOrganisaatioActor(organisaatioClient, config)), "organisaatio")
  val henkilo = system.actorOf(Props(new fi.vm.sade.hakurekisteri.integration.henkilo.HttpHenkiloActor(henkiloClient, config)), "henkilo")
  val hakemukset = system.actorOf(Props(new HakemusActor(
    hakemusClient,
    config.integrations.hakemusConfig.maxApplications
  )).withDispatcher("akka.hakurekisteri.query-prio-dispatcher"), "hakemus")
  val koodisto = system.actorOf(Props(new KoodistoActor(koodistoClient, config)), "koodisto")
  val parametrit = system.actorOf(Props(new HttpParameterActor(parametritClient)), "parametrit")
  val valintaTulos = system.actorOf(Props(new ValintaTulosActor(valintatulosClient, config)), "valintaTulos")
  val valintarekisteri = system.actorOf(Props(new ValintarekisteriActor(valintarekisteriClient, config)), "valintarekisteri")
  val ytl = system.actorOf(Props(new YtlActor(
    henkilo,
    rekisterit.suoritusRekisteri,
    rekisterit.arvosanaRekisteri,
    hakemukset,
    config.integrations.ytlConfig
  )), "ytl")
  private val virtaClient = new VirtaClient(
    config = config.integrations.virtaConfig,
    apiVersion = config.properties.getOrElse("suoritusrekisteri.virta.apiversio", VirtaClient.version105)
  )(virtaEc, system)
  private val virtaResourceClient = new VirtaClient(
    config = config.integrations.virtaConfig,
    apiVersion = config.properties.getOrElse("suoritusrekisteri.virta.apiversio", VirtaClient.version106)
  )(virtaResourceEc, system)
  val virta = system.actorOf(Props(new VirtaActor(virtaClient, organisaatiot, rekisterit.suoritusRekisteri, rekisterit.opiskeluoikeusRekisteri)), "virta")
  val virtaResource = system.actorOf(Props(new VirtaResourceActor(virtaResourceClient)), "virtaResource")
  val proxies = new HttpProxies(valintarekisteriClient)

  hakemukset ! IlmoitetutArvosanatTrigger(rekisterit.suoritusRekisteri, rekisterit.arvosanaRekisteri)(system.dispatcher)

  override val hakuAppPermissionChecker: ActorRef = system.actorOf(Props(new HakuAppPermissionCheckerActor(hakuAppPermissionCheckerClient, organisaatiot)))
}