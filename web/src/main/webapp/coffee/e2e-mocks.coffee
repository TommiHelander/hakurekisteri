angular.module('e2e-mocks', ['ngMockE2E'])
.run ($httpBackend) ->
  window.httpBackend = $httpBackend

  #Local templates
  $httpBackend.when('GET', /templates\/.*/).passThrough()

  $httpBackend.when('GET', /.*\/authentication-service\/buildversion\.txt\?auth/).respond("artifactId=authentication-service
                 version=13.0-MOCK
                 buildNumber=99
                 branchName=master
                 vcsRevision=xxx
                 buildTtime=20150227-0927")
  $httpBackend.when('GET', /.*\/cas\/myroles/).respond(["USER_robotti", "APP_HENKILONHALLINTA_CRUD", "APP_OID", "APP_HENKILONHALLINTA", "APP_ORGANISAATIOHALLINTA", "APP_KOODISTO_READ", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_READ", "APP_TARJONTA_CRUD", "APP_HENKILONHALLINTA_OPHREKISTERI", "APP_KOOSTEROOLIENHALLINTA_READ", "APP_KOODISTO", "APP_ANOMUSTENHALLINTA", "APP_OMATTIEDOT", "APP_TARJONTA", "APP_HAKUJENHALLINTA", "APP_OMATTIEDOT_READ_UPDATE", "APP_RAPORTOINTI", "APP_ANOMUSTENHALLINTA_CRUD", "APP_HAKUJENHALLINTA_CRUD", "VIRKAILIJA", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA", "APP_KOOSTEROOLIENHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD", "APP_OID_READ", "APP_HENKILONHALLINTA_READ", "APP_SIJOITTELU", "APP_HAKUJENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_LOKALISOINTI_CRUD_1.2.246.562.10.00000000001", "APP_YHTEYSTIETOTYYPPIENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEET_CRUD", "APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.00000000001", "APP_KOODISTO_READ_1.2.246.562.10.00000000001", "APP_OMATTIEDOT_CRUD_1.2.246.562.10.00000000001", "APP_TARJONTA_READ_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_CRUD", "APP_AITU_CRUD", "APP_VALINTOJENTOTEUTTAMINEN_CRUD_1.2.246.562.10.00000000001", "APP_ANOMUSTENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_LOKALISOINTI", "APP_SISALLONHALLINTA", "APP_TARJONTA_CRUD_1.2.246.562.10.00000000001", "APP_ORGANISAATIOHALLINTA_READ", "APP_TIEDONSIIRTO_CRUD_1.2.246.562.10.00000000001", "APP_OMATTIEDOT_CRUD", "APP_OID_CRUD", "APP_KOOSTEROOLIENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_KOODISTO_CRUD_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA_READ_1.2.246.562.10.00000000001", "APP_SISALLONHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_ANOMUSTENHALLINTA_READ", "APP_YHTEYSTIETOTYYPPIENHALLINTA_CRUD", "APP_KOODISTO_CRUD", "APP_AITU_CRUD_1.2.246.562.10.00000000001", "APP_ANOMUSTENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINEN", "APP_SUORITUSREKISTERI_CRUD", "APP_HAKEMUS_READ_UPDATE", "APP_HAKUJENHALLINTA_READ", "APP_RYHMASAHKOPOSTI_VIEW_1.2.246.562.10.00000000001", "APP_HAKULOMAKKEENHALLINTA", "APP_TIEDONSIIRTO_CRUD", "APP_SISALLONHALLINTA_CRUD", "APP_HAKEMUS", "APP_HAKEMUS_CRUD_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_HAKEMUS_CRUD", "APP_OSOITE_CRUD", "APP_YHTEYSTIETOTYYPPIENHALLINTA", "APP_OMATTIEDOT_READ_UPDATE_1.2.246.562.10.00000000001", "APP_ORGANISAATIOHALLINTA_READ_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_HAKULOMAKKEENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_OSOITE_CRUD_1.2.246.562.10.00000000001", "APP_RYHMASAHKOPOSTI", "APP_SIJOITTELU_CRUD_1.2.246.562.10.00000000001", "APP_LOKALISOINTI_CRUD", "APP_RYHMASAHKOPOSTI_SEND_1.2.246.562.10.00000000001", "APP_OID_READ_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA_OPHREKISTERI_1.2.246.562.10.00000000001", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_YHTEYSTIETOTYYPPIENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_KOOSTEROOLIENHALLINTA_CRUD", "APP_AITU", "APP_SUORITUSREKISTERI", "APP_TIEDONSIIRTO", "APP_YHTEYSTIETOTYYPPIENHALLINTA_READ", "APP_VALINTAPERUSTEET", "APP_TARJONTA_READ", "APP_VALINTOJENTOTEUTTAMINEN_CRUD", "APP_OID_CRUD_1.2.246.562.10.00000000001", "APP_HAKEMUS_READ_UPDATE_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEET_CRUD_1.2.246.562.10.00000000001", "APP_KOOSTEROOLIENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_SIJOITTELU_CRUD", "APP_HAKUJENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_RYHMASAHKOPOSTI_SEND", "APP_HAKULOMAKKEENHALLINTA_CRUD", "APP_RYHMASAHKOPOSTI_VIEW", "APP_OSOITE", "APP_HAKEMUS_LISATIETOCRUD_1.2.246.562.10.00000000001", "APP_ASIAKIRJAPALVELU_CREATE_TEMPLATE_1.2.246.562.10.00000000001", "APP_IPOSTI_SEND", "APP_VALINTAPERUSTEETKK_CRUD", "APP_ASIAKIRJAPALVELU_READ", "APP_ASIAKIRJAPALVELU_CREATE_TEMPLATE", "APP_IPOSTI_READ_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINENKK_CRUD_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINENKK_CRUD", "APP_ASIAKIRJAPALVELU_ASIOINTITILICRUD_1.2.246.562.10.00000000001", "APP_RAPORTOINTI_CRUD", "APP_IPOSTI_SEND_1.2.246.562.10.00000000001", "APP_KKHAKUVIRKAILIJA_CRUD_1.2.246.562.10.00000000001", "APP_IPOSTI", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_KK", "APP_IPOSTI_READ", "APP_VALINTOJENTOTEUTTAMINENKK", "APP_ASIAKIRJAPALVELU_CREATE_LETTER_1.2.246.562.10.00000000001", "APP_ASIAKIRJAPALVELU", "APP_KKHAKUVIRKAILIJA_CRUD", "APP_TARJONTA_KK_CRUD", "APP_ASIAKIRJAPALVELU_CREATE_LETTER", "APP_TARJONTA_KK_CRUD_1.2.246.562.10.00000000001", "APP_TARJONTA_KK", "APP_HAKEMUS_LISATIETOCRUD", "APP_VALINTAPERUSTEETKK_CRUD_1.2.246.562.10.00000000001", "APP_ASIAKIRJAPALVELU_ASIOINTITILICRUD", "APP_KKHAKUVIRKAILIJA", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_KK_CRUD", "APP_RAPORTOINTI_CRUD_1.2.246.562.10.00000000001", "APP_ASIAKIRJAPALVELU_READ_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEETKK", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_KK_CRUD_1.2.246.562.10.00000000001", "APP_ASIAKIRJAPALVELU_SEND_LETTER_EMAIL", "APP_ASIAKIRJAPALVELU_SEND_LETTER_EMAIL_1.2.246.562.10.00000000001", "APP_TIEDONSIIRTO_VALINTA_1.2.246.562.10.00000000001", "APP_TIEDONSIIRTO_VALINTA", "APP_VALINTOJENTOTEUTTAMINEN_TULOSTENTUONTI_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINEN_TULOSTENTUONTI", "APP_HAKULOMAKKEENHALLINTA_LOMAKEPOHJANVAIHTO", "APP_HAKULOMAKKEENHALLINTA_LOMAKEPOHJANVAIHTO_1.2.246.562.10.00000000001"])
  window.runTestHooks?()