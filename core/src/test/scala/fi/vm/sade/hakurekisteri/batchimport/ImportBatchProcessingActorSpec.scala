package fi.vm.sade.hakurekisteri.batchimport

import java.util.UUID

import akka.actor.{Props, ActorSystem}
import com.ning.http.client.AsyncHttpClient
import fi.vm.sade.hakurekisteri.arvosana.Arvosana
import fi.vm.sade.hakurekisteri.integration.organisaatio.OrganisaatioActor
import fi.vm.sade.hakurekisteri.integration._
import fi.vm.sade.hakurekisteri.integration.henkilo.{CreateHenkilo, Henkilo, HenkiloActor}
import fi.vm.sade.hakurekisteri.opiskelija.Opiskelija
import fi.vm.sade.hakurekisteri.rest.support.{HakurekisteriJsonSupport, Query}
import fi.vm.sade.hakurekisteri.storage.Identified
import fi.vm.sade.hakurekisteri.suoritus.{VirallinenSuoritus, Suoritus}
import fi.vm.sade.hakurekisteri.test.tools.MockedResourceActor
import org.mockito.Mockito._
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.concurrent.AsyncAssertions
import org.scalatest.mock.MockitoSugar

import scala.concurrent.ExecutionContext


class ImportBatchProcessingActorSpec extends FlatSpec with Matchers with MockitoSugar with DispatchSupport with AsyncAssertions with HakurekisteriJsonSupport {
  implicit val system = ActorSystem("test-import-batch-processing")
  implicit val ec: ExecutionContext = system.dispatcher

  behavior of "ImportBatchProcessingActor"

  val lahde = "testitiedonsiirto"

  val batch: ImportBatch with Identified[UUID] = ImportBatch(<perustiedot>
    <eranTunniste>eranTunniste</eranTunniste>
    <henkilot>
      <henkilo>
        <hetu>111111-1975</hetu>
        <lahtokoulu>05127</lahtokoulu>
        <luokka>9A</luokka>
        <sukunimi>Testinen</sukunimi>
        <etunimet>Juha Jaakko</etunimet>
        <kutsumanimi>Jaakko</kutsumanimi>
        <kotikunta>020</kotikunta>
        <aidinkieli>FI</aidinkieli>
        <kansalaisuus>246</kansalaisuus>
        <lahiosoite>Katu 1 A 1</lahiosoite>
        <postinumero>00100</postinumero>
        <matkapuhelin>040 1234 567</matkapuhelin>
        <muuPuhelin>09 1234 567</muuPuhelin>
        <perusopetus>
          <valmistuminen>2015-06-04</valmistuminen>
          <myontaja>05127</myontaja>
          <suorituskieli>FI</suorituskieli>
          <tila>KESKEN</tila>
          <yksilollistaminen>EI</yksilollistaminen>
        </perusopetus>
      </henkilo>
      <henkilo>
        <henkiloTunniste>TUNNISTE</henkiloTunniste>
        <syntymaAika>1999-03-29</syntymaAika>
        <sukupuoli>1</sukupuoli>
        <lahtokoulu>05127</lahtokoulu>
        <luokka>9A</luokka>
        <sukunimi>Testinen</sukunimi>
        <etunimet>Juha Jaakko</etunimet>
        <kutsumanimi>Jaakko</kutsumanimi>
        <kotikunta>020</kotikunta>
        <aidinkieli>FI</aidinkieli>
        <kansalaisuus>246</kansalaisuus>
        <lahiosoite>Katu 1 A 1</lahiosoite>
        <postinumero>00100</postinumero>
        <matkapuhelin>040 1234 567</matkapuhelin>
        <muuPuhelin>09 1234 567</muuPuhelin>
        <ulkomainen>
          <valmistuminen>2014-06-04</valmistuminen>
          <myontaja>05127</myontaja>
          <suorituskieli>FI</suorituskieli>
          <tila>KESKEN</tila>
        </ulkomainen>
        <maahanmuuttajienammvalmistava>
          <valmistuminen>2015-06-04</valmistuminen>
          <myontaja>05127</myontaja>
          <suorituskieli>FI</suorituskieli>
          <tila>VALMIS</tila>
        </maahanmuuttajienammvalmistava>
      </henkilo>
    </henkilot>
  </perustiedot>, Some("foo"), "perustiedot", lahde, BatchState.READY, ImportStatus()).identify(UUID.randomUUID())

  def createEndpoint = {
    val result = mock[Endpoint]

    val henkiloBody = {
      val oidResolver = (koodi: String) => s"1.2.246.562.5.$koodi"
      val henkilo: CreateHenkilo = (batch.data \ "henkilot" \ "henkilo").map(ImportHenkilo(_)(lahde)).head.toHenkilo(oidResolver)
      import org.json4s.jackson.Serialization.write
      write[CreateHenkilo](henkilo)
    }

    when(result.request(forUrl("http://localhost/authentication-service/resources/s2s/tiedonsiirrot"))).thenReturn((200, List(), "1.2.246.562.24.123"))

    when(result.request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&suunnitellut=true"))).thenReturn((200, List(), "{\"numHits\":1,\"organisaatiot\":[{\"oid\":\"1.2.246.562.5.05127\",\"nimi\":{},\"oppilaitosKoodi\":\"05127\"}]}"))

    when(result.request(forUrl("http://localhost/organisaatio-service/rest/organisaatio/05127"))).thenReturn((200, List(), "{\"oid\":\"1.2.246.562.5.05127\",\"nimi\":{},\"oppilaitosKoodi\":\"05127\"}"))

    result
  }
  val endpoint = createEndpoint
  val asyncProvider = new CapturingProvider(endpoint)


  it should "import data into henkilopalvelu and suoritusrekisteri" in {
    val importBatchActor = system.actorOf(Props(new MockedResourceActor[ImportBatch](save = {r =>}, query = { (q) => Seq(batch) })))
    val henkiloClient = new VirkailijaRestClient(ServiceConfig(serviceUrl = "http://localhost/authentication-service"), Some(new AsyncHttpClient(asyncProvider)))
    val henkiloActor = system.actorOf(Props(new HenkiloActor(henkiloClient)))

    val sWaiter = new Waiter()
    val oWaiter = new Waiter()
    val suoritusHandler = (suoritus: Suoritus) => {
      sWaiter { suoritus.asInstanceOf[VirallinenSuoritus].myontaja should be ("1.2.246.562.5.05127") }
      sWaiter.dismiss()
    }
    val opiskelijaHandler = (o: Opiskelija) => {
      oWaiter { o.oppilaitosOid should be ("1.2.246.562.5.05127") }
      oWaiter.dismiss()
    }
    val suoritusrekisteri = system.actorOf(Props(new MockedResourceActor[Suoritus](save = suoritusHandler, query = {q => Seq()})))
    val opiskelijarekisteri = system.actorOf(Props(new MockedResourceActor[Opiskelija](save = opiskelijaHandler, query = {q => Seq()})))
    val organisaatioClient = new VirkailijaRestClient(ServiceConfig(serviceUrl = "http://localhost/organisaatio-service"), Some(new AsyncHttpClient(asyncProvider)))
    val organisaatioActor = system.actorOf(Props(new OrganisaatioActor(organisaatioClient)))
    val koodistoActor = system.actorOf(Props(new MockedKoodistoActor()))
    val arvosanarekisteri = system.actorOf(Props(new MockedResourceActor[Arvosana](save = {r => r}, query = { (q) => Seq() })))

    val processingActor = system.actorOf(Props(new ImportBatchProcessingActor(importBatchActor, henkiloActor, suoritusrekisteri, opiskelijarekisteri, organisaatioActor, arvosanarekisteri, koodistoActor)))

    processingActor ! ProcessReadyBatches

    import org.scalatest.time.SpanSugar._

    sWaiter.await(timeout(30.seconds), dismissals(1))
    oWaiter.await(timeout(30.seconds), dismissals(1))

    system.shutdown()
    system.awaitTermination()
  }

}
