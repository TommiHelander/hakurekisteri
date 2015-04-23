package fi.vm.sade.hakurekisteri.web.proxies

import akka.actor.ActorSystem
import fi.vm.sade.hakurekisteri.Config
import fi.vm.sade.hakurekisteri.integration.VirkailijaRestClient
import org.scalatra.{AsyncResult, FutureSupport, ScalatraServlet}

import scala.concurrent.{ExecutionContext, Future}

class OrganizationProxyResource(config: Config, system: ActorSystem) extends ScalatraServlet with FutureSupport {
  implicit val executor: ExecutionContext = system.dispatcher

  val proxy = config.mockMode match {
    case true => new MockOrganizationProxy
    case false => new HttpOrganizationProxy(config, system)
  }

  before() {
    contentType = "application/json"
  }

  get("/rest/organisaatio/v2/hae") {
    new AsyncResult() {
      val is = proxy.search(request.getQueryString).map(_.getOrElse("""{"numHits":0,"organisaatiot":[]}"""))
    }
  }

  get("/rest/organisaatio/:oid") {
    new AsyncResult() {
      val is = proxy.get(params("oid")).map(_.getOrElse("???"))
    }
  }
}

trait OrganizationProxy {
  def search(query: String): Future[Option[String]]
  def get(oid: String): Future[Option[String]]
}

class MockOrganizationProxy extends OrganizationProxy {
  def search(query: String) = Future.successful( Some("""{"numHits":1,"organisaatiot":[{"oid":"1.2.246.562.10.39644336305","alkuPvm":694216800000,"parentOid":"1.2.246.562.10.80381044462","parentOidPath":"1.2.246.562.10.39644336305/1.2.246.562.10.80381044462/1.2.246.562.10.00000000001","oppilaitosKoodi":"06345","oppilaitostyyppi":"oppilaitostyyppi_11#1","match":true,"nimi":{"fi":"Pikkaralan ala-aste"},"kieletUris":["oppilaitoksenopetuskieli_1#1"],"kotipaikkaUri":"kunta_564","children":[],"organisaatiotyypit":["OPPILAITOS"],"aliOrganisaatioMaara":0}]}""") )
  def get(oid: String) = Future.successful(Some("""{"oid":"1.2.246.562.10.39644336305","nimi":{"fi":"Pikkaralan ala-aste"},"alkuPvm":"1992-01-01","postiosoite":{"osoiteTyyppi":"posti","yhteystietoOid":"1.2.246.562.5.75344290822","postinumeroUri":"posti_90310","osoite":"Vasantie 121","postitoimipaikka":"OULU","ytjPaivitysPvm":null,"lng":null,"lap":null,"coordinateType":null,"osavaltio":null,"extraRivi":null,"maaUri":null},"parentOid":"1.2.246.562.10.80381044462","parentOidPath":"|1.2.246.562.10.00000000001|1.2.246.562.10.80381044462|","vuosiluokat":[],"oppilaitosKoodi":"06345","kieletUris":["oppilaitoksenopetuskieli_1#1"],"oppilaitosTyyppiUri":"oppilaitostyyppi_11#1","yhteystiedot":[{"kieli":"kieli_fi#1","id":"22913","yhteystietoOid":"1.2.246.562.5.11296174961","email":"kaisa.tahtinen@ouka.fi"},{"tyyppi":"faksi","kieli":"kieli_fi#1","id":"22914","yhteystietoOid":"1.2.246.562.5.18105745956","numero":"08  5586 1582"},{"tyyppi":"puhelin","kieli":"kieli_fi#1","id":"22915","yhteystietoOid":"1.2.246.562.5.364178776310","numero":"08  5586 9514"},{"kieli":"kieli_fi#1","id":"22916","yhteystietoOid":"1.2.246.562.5.94533742915","www":"http://www.edu.ouka.fi/koulut/pikkarala"},{"osoiteTyyppi":"posti","kieli":"kieli_fi#1","id":"22917","yhteystietoOid":"1.2.246.562.5.75344290822","osoite":"Vasantie 121","postinumeroUri":"posti_90310","postitoimipaikka":"OULU","ytjPaivitysPvm":null,"coordinateType":null,"lap":null,"lng":null,"osavaltio":null,"extraRivi":null,"maaUri":null},{"osoiteTyyppi":"kaynti","kieli":"kieli_fi#1","id":"22918","yhteystietoOid":"1.2.246.562.5.58988409759","osoite":"Vasantie 121","postinumeroUri":"posti_90310","postitoimipaikka":"OULU","ytjPaivitysPvm":null,"coordinateType":null,"lap":null,"lng":null,"osavaltio":null,"extraRivi":null,"maaUri":null}],"kuvaus2":{},"tyypit":["Oppilaitos"],"yhteystietoArvos":[],"nimet":[{"nimi":{"fi":"Pikkaralan ala-aste"},"alkuPvm":"1992-01-01","version":1}],"kayntiosoite":{"osoiteTyyppi":"kaynti","yhteystietoOid":"1.2.246.562.5.58988409759","postinumeroUri":"posti_90310","osoite":"Vasantie 121","postitoimipaikka":"OULU","ytjPaivitysPvm":null,"lng":null,"lap":null,"coordinateType":null,"osavaltio":null,"extraRivi":null,"maaUri":null},"kotipaikkaUri":"kunta_564","maaUri":"maatjavaltiot1_fin","ryhmatyypit":[],"kayttoryhmat":[],"version":1,"status":"AKTIIVINEN"}"""))
}

class HttpOrganizationProxy(config: Config, system: ActorSystem) extends OrganizationProxy {
  implicit val ec: ExecutionContext = system.dispatcher
  // TODO: not nice to duplicate the rest client here
  val organisaatioClient = new VirkailijaRestClient(config.integrations.organisaatioConfig, None)(ec, system) {
    override def serviceName = Some("organisaatio-proxy")
  }

  def search(query: String) = {
    organisaatioClient.readObject[String]("/rest/organisaatio/v2/hae?" + query, 200, 1).map(Option(_))
  }

  def get(oid: String) = {
    organisaatioClient.readObject[String]("/rest/organisaatio/" + oid, 200, 1).map(Option(_))
  }
}