package fi.vm.sade.hakurekisteri.web.proxies

import javax.servlet.ServletContext
import akka.actor.ActorSystem
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriJsonSupport
import org.scalatra.{InternalServerError, FutureSupport, ScalatraServlet, AsyncResult}
import org.scalatra.servlet.ServletApiImplicits
import ServletApiImplicits._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext

object ProxyServlets {
  def mount(proxies: Proxies, context: ServletContext)(implicit system: ActorSystem) {
    context.mount(new OrganizationProxyServlet(proxies.organization, system), "/organisaatio-service")
    context.mount(new AuthenticationProxyServlet(proxies.authentication, system), "/authentication-service")
    context.mount(new KoodistoProxyServlet(proxies.koodisto, system), "/koodisto-service")
    context.mount(new LocalizationMockServlet(system), "/lokalisointi")
    context.mount(new CasMockServlet(system), "/cas")
  }
}

class OrganizationProxyServlet(proxy: OrganizationProxy, system: ActorSystem) extends OPHProxyServlet(system) {
  get("/rest/organisaatio/v2/hae") {
    new AsyncResult() {
      val is = proxy.search(request.getQueryString)
    }
  }

  get("/rest/organisaatio/:oid") {
    new AsyncResult() {
      val is = proxy.get(params("oid"))
    }
  }
}

class AuthenticationProxyServlet(proxy: AuthenticationProxy, system: ActorSystem) extends OPHProxyServlet(system) with HakurekisteriJsonSupport {
  get("/buildversion.txt") {
    contentType = "text/plain"
    "artifactId=authentication-service\nmocked"
  }

  get("/resources/henkilo/:oid") {
    new AsyncResult() {
      val is = proxy.henkiloByOid(params("oid"))
    }
  }

  post("/resources/henkilo/henkilotByHenkiloOidList") {
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    new AsyncResult() {
      val parsedBody = parse(request.body)
      val is = proxy.henkilotByOidList(parsedBody.extract[List[String]])
    }
  }
}

class KoodistoProxyServlet(proxy: KoodistoProxy, system: ActorSystem) extends OPHProxyServlet(system) with HakurekisteriJsonSupport {
  get("/rest/json/:id/koodi*") {
    proxy.koodi(params("id"))
  }
}

class LocalizationMockServlet(system: ActorSystem) extends OPHProxyServlet(system) {
  get("/cxf/rest/v1/localisation") {
    getClass.getResourceAsStream("/proxy-mockdata/localization.json")
  }
}

class CasMockServlet(system: ActorSystem) extends OPHProxyServlet(system) {
  get("/myroles") {
    """["USER_ophadmin", "VIRKAILIJA", "STRONG_AUTHENTICATED_FOO", "STRONG_AUTHENTICATED", "LANG_fi", "APP_AITU", "APP_AITU_READ", "APP_AITU_READ_1.2.246.562.10.00000000001", "APP_KOODISTO", "APP_KOODISTO_READ", "APP_KOODISTO_READ_1.2.246.562.10.00000000001", "APP_KOODISTO", "APP_KOODISTO_READ_UPDATE", "APP_KOODISTO_READ_UPDATE_1.2.246.562.10.00000000001", "APP_AITU", "APP_AITU_CRUD", "APP_AITU_CRUD_1.2.246.562.10.00000000001", "APP_KOODISTO", "APP_KOODISTO_CRUD", "APP_KOODISTO_CRUD_1.2.246.562.10.00000000001", "APP_AITU", "APP_AITU_READ_UPDATE", "APP_AITU_READ_UPDATE_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA", "APP_HENKILONHALLINTA_2ASTEENVASTUU", "APP_HENKILONHALLINTA_2ASTEENVASTUU_1.2.246.562.10.00000000001", "APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_READ", "APP_ORGANISAATIOHALLINTA_READ_1.2.246.562.10.00000000001", "APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_READ_UPDATE", "APP_ORGANISAATIOHALLINTA_READ_UPDATE_1.2.246.562.10.00000000001", "APP_SIJOITTELU", "APP_SIJOITTELU_READ", "APP_SIJOITTELU_READ_1.2.246.562.10.00000000001", "APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA", "APP_HENKILONHALLINTA_OPOTVIRKAILIJAT", "APP_HENKILONHALLINTA_OPOTVIRKAILIJAT_1.2.246.562.10.00000000001", "APP_OID", "APP_OID_READ", "APP_OID_READ_1.2.246.562.10.00000000001", "APP_OID", "APP_OID_READ_UPDATE", "APP_OID_READ_UPDATE_1.2.246.562.10.00000000001", "APP_RYHMASAHKOPOSTI", "APP_RYHMASAHKOPOSTI_SEND", "APP_RYHMASAHKOPOSTI_SEND_1.2.246.562.10.00000000001", "APP_OID", "APP_OID_CRUD", "APP_OID_CRUD_1.2.246.562.10.00000000001", "APP_SIJOITTELU", "APP_SIJOITTELU_READ_UPDATE", "APP_SIJOITTELU_READ_UPDATE_1.2.246.562.10.00000000001", "APP_OMATTIEDOT", "APP_OMATTIEDOT_READ", "APP_OMATTIEDOT_READ_1.2.246.562.10.00000000001", "APP_OMATTIEDOT", "APP_OMATTIEDOT_READ_UPDATE", "APP_OMATTIEDOT_READ_UPDATE_1.2.246.562.10.00000000001", "APP_SIJOITTELU", "APP_SIJOITTELU_CRUD", "APP_SIJOITTELU_CRUD_1.2.246.562.10.00000000001", "APP_OMATTIEDOT", "APP_OMATTIEDOT_CRUD", "APP_OMATTIEDOT_CRUD_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA", "APP_HENKILONHALLINTA_READ", "APP_HENKILONHALLINTA_READ_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA", "APP_HENKILONHALLINTA_READ_UPDATE", "APP_HENKILONHALLINTA_READ_UPDATE_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA", "APP_HENKILONHALLINTA_CRUD", "APP_HENKILONHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_ANOMUSTENHALLINTA", "APP_ANOMUSTENHALLINTA_READ", "APP_ANOMUSTENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_ANOMUSTENHALLINTA", "APP_ANOMUSTENHALLINTA_READ_UPDATE", "APP_ANOMUSTENHALLINTA_READ_UPDATE_1.2.246.562.10.00000000001", "APP_ANOMUSTENHALLINTA", "APP_ANOMUSTENHALLINTA_CRUD", "APP_ANOMUSTENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_KOOSTEROOLIENHALLINTA", "APP_KOOSTEROOLIENHALLINTA_READ", "APP_KOOSTEROOLIENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_KOOSTEROOLIENHALLINTA", "APP_KOOSTEROOLIENHALLINTA_READ_UPDATE", "APP_KOOSTEROOLIENHALLINTA_READ_UPDATE_1.2.246.562.10.00000000001", "APP_KOOSTEROOLIENHALLINTA", "APP_KOOSTEROOLIENHALLINTA_CRUD", "APP_KOOSTEROOLIENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_TARJONTA", "APP_TARJONTA_READ", "APP_TARJONTA_READ_1.2.246.562.10.00000000001", "APP_TARJONTA", "APP_TARJONTA_READ_UPDATE", "APP_TARJONTA_READ_UPDATE_1.2.246.562.10.00000000001", "APP_HAKULOMAKKEENHALLINTA", "APP_HAKULOMAKKEENHALLINTA_READ", "APP_HAKULOMAKKEENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_TARJONTA", "APP_TARJONTA_CRUD", "APP_TARJONTA_CRUD_1.2.246.562.10.00000000001", "APP_HAKUJENHALLINTA", "APP_HAKUJENHALLINTA_READ", "APP_HAKUJENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_HAKUJENHALLINTA", "APP_HAKUJENHALLINTA_READ_UPDATE", "APP_HAKUJENHALLINTA_READ_UPDATE_1.2.246.562.10.00000000001", "APP_HAKULOMAKKEENHALLINTA", "APP_HAKULOMAKKEENHALLINTA_CRUD", "APP_HAKULOMAKKEENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_HAKULOMAKKEENHALLINTA", "APP_HAKULOMAKKEENHALLINTA_READ_UPDATE", "APP_HAKULOMAKKEENHALLINTA_READ_UPDATE_1.2.246.562.10.00000000001", "APP_HAKUJENHALLINTA", "APP_HAKUJENHALLINTA_CRUD", "APP_HAKUJENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_YHTEYSTIETOTYYPPIENHALLINTA", "APP_YHTEYSTIETOTYYPPIENHALLINTA_READ", "APP_YHTEYSTIETOTYYPPIENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_YHTEYSTIETOTYYPPIENHALLINTA", "APP_YHTEYSTIETOTYYPPIENHALLINTA_READ_UPDATE", "APP_YHTEYSTIETOTYYPPIENHALLINTA_READ_UPDATE_1.2.246.562.10.00000000001", "APP_LOKALISOINTI", "APP_LOKALISOINTI_READ", "APP_LOKALISOINTI_READ_1.2.246.562.10.00000000001", "APP_YHTEYSTIETOTYYPPIENHALLINTA", "APP_YHTEYSTIETOTYYPPIENHALLINTA_CRUD", "APP_YHTEYSTIETOTYYPPIENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_LOKALISOINTI", "APP_LOKALISOINTI_READ_UPDATE", "APP_LOKALISOINTI_READ_UPDATE_1.2.246.562.10.00000000001", "APP_LOKALISOINTI", "APP_LOKALISOINTI_CRUD", "APP_LOKALISOINTI_CRUD_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_READ", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_SISALLONHALLINTA", "APP_SISALLONHALLINTA_READ", "APP_SISALLONHALLINTA_READ_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_READ_UPDATE", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_READ_UPDATE_1.2.246.562.10.00000000001", "APP_SISALLONHALLINTA", "APP_SISALLONHALLINTA_READ_UPDATE", "APP_SISALLONHALLINTA_READ_UPDATE_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_CRUD", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_SISALLONHALLINTA", "APP_SISALLONHALLINTA_CRUD", "APP_SISALLONHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_OSOITE", "APP_OSOITE_CRUD", "APP_OSOITE_CRUD_1.2.246.562.10.00000000001", "APP_HAKEMUS", "APP_HAKEMUS_READ", "APP_HAKEMUS_READ_1.2.246.562.10.00000000001", "APP_HAKEMUS", "APP_HAKEMUS_READ_UPDATE", "APP_HAKEMUS_READ_UPDATE_1.2.246.562.10.00000000001", "APP_HAKEMUS", "APP_HAKEMUS_CRUD", "APP_HAKEMUS_CRUD_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEET", "APP_VALINTAPERUSTEET_READ", "APP_VALINTAPERUSTEET_READ_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEET", "APP_VALINTAPERUSTEET_READ_UPDATE", "APP_VALINTAPERUSTEET_READ_UPDATE_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEET", "APP_VALINTAPERUSTEET_CRUD", "APP_VALINTAPERUSTEET_CRUD_1.2.246.562.10.00000000001", "APP_TIEDONSIIRTO", "APP_TIEDONSIIRTO_READ", "APP_TIEDONSIIRTO_READ_1.2.246.562.10.00000000001", "APP_TIEDONSIIRTO", "APP_TIEDONSIIRTO_CRUD", "APP_TIEDONSIIRTO_CRUD_1.2.246.562.10.00000000001", "APP_TIEDONSIIRTO", "APP_TIEDONSIIRTO_READ_UPDATE", "APP_TIEDONSIIRTO_READ_UPDATE_1.2.246.562.10.00000000001", "APP_SUORITUSREKISTERI", "APP_SUORITUSREKISTERI_READ", "APP_SUORITUSREKISTERI_READ_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINEN", "APP_VALINTOJENTOTEUTTAMINEN_READ", "APP_VALINTOJENTOTEUTTAMINEN_READ_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA", "APP_HENKILONHALLINTA_OPHREKISTERI", "APP_HENKILONHALLINTA_OPHREKISTERI_1.2.246.562.10.00000000001", "APP_SUORITUSREKISTERI", "APP_SUORITUSREKISTERI_CRUD", "APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.00000000001", "APP_SUORITUSREKISTERI", "APP_SUORITUSREKISTERI_READ_UPDATE", "APP_SUORITUSREKISTERI_READ_UPDATE_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINEN", "APP_VALINTOJENTOTEUTTAMINEN_READ_UPDATE", "APP_VALINTOJENTOTEUTTAMINEN_READ_UPDATE_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA", "APP_HENKILONHALLINTA_KKVASTUU", "APP_HENKILONHALLINTA_KKVASTUU_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINEN", "APP_VALINTOJENTOTEUTTAMINEN_CRUD", "APP_VALINTOJENTOTEUTTAMINEN_CRUD_1.2.246.562.10.00000000001", "APP_EPERUSTEET", "APP_EPERUSTEET_CRUD", "APP_EPERUSTEET_CRUD_1.2.246.562.28.11287634288", "APP_EPERUSTEET", "APP_EPERUSTEET_CRUD", "APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001", "APP_RYHMASAHKOPOSTI", "APP_RYHMASAHKOPOSTI_SEND", "APP_RYHMASAHKOPOSTI_SEND_1.2.246.562.10.00000000001", "APP_RYHMASAHKOPOSTI", "APP_RYHMASAHKOPOSTI_VIEW", "APP_RYHMASAHKOPOSTI_VIEW_1.2.246.562.10.00000000001"]"""
  }
}

protected class OPHProxyServlet(system: ActorSystem) extends ScalatraServlet with FutureSupport {
  implicit val executor: ExecutionContext = system.dispatcher
  val log = LoggerFactory.getLogger(getClass)

  before() {
    contentType = "application/json"
  }

  error { case x: Throwable =>
    log.error("OPH proxy fail", x)
    InternalServerError()
  }
}