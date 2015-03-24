angular.module('e2e-mocks', ['ngMockE2E'])
.run ($httpBackend) ->
  window.httpBackend = $httpBackend

  #Local templates
  $httpBackend.when('GET', /templates\/.*/).passThrough()

  # Mocked local services
  $httpBackend.when('GET', /.*rest\/v1\/rekisteritiedot\/light\?oppilaitosOid=1\.2\.246\.562\.10\.39644336305&vuosi=2015$/).respond([{"henkilo":"1.2.246.562.24.71944845619","luokka":"9A","arvosanat":true},{"henkilo":"1.2.246.562.24.49719248091","luokka":"9A","arvosanat":true},{"henkilo":"1.2.246.562.24.76359038731","luokka":"9A","arvosanat":true},{"henkilo":"1.2.246.562.24.87951154293","luokka":"9A","arvosanat":true},{"henkilo":"1.2.246.562.24.98743797763","luokka":"9A","arvosanat":true}])

  $httpBackend.when('GET', /.*rest\/v1\/opiskelijat\?henkilo=1.2.246.562.24.71944845619.*/).respond([{"id":"e86fb63a-607a-48da-b701-4527193e9efc","oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","luokka":"9A","henkiloOid":"1.2.246.562.24.71944845619","alkuPaiva":"2014-08-17T21:00:00.000Z","loppuPaiva":"2015-06-03T21:00:00.000Z","source":"Test","core":{"oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","henkiloOid":"1.2.246.562.24.71944845619"}}])
  $httpBackend.when('GET', /.*rest\/v1\/opiskelijat\?henkilo=1.2.246.562.24.98743797763$/).respond([{"id":"67108ac8-11db-4ec1-b1a5-613ffd095251","oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","luokka":"9A","henkiloOid":"1.2.246.562.24.98743797763","alkuPaiva":"2014-08-17T21:00:00.000Z","loppuPaiva":"2015-06-03T21:00:00.000Z","source":"Test","core":{"oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","henkiloOid":"1.2.246.562.24.98743797763"}}])
  $httpBackend.when('GET', /.*rest\/v1\/opiskelijat\?henkilo=123456-789&vuosi=2015$/).respond([{"id":"6812d1cb-bc15-435a-ab0c-53414d1a7775","oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","luokka":"9A","henkiloOid":"1.2.246.562.24.71944845619","alkuPaiva":"2014-08-17T21:00:00.000Z","loppuPaiva":"2015-06-03T21:00:00.000Z","source":"Test","core":{"oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","henkiloOid":"1.2.246.562.24.71944845619"}}])

  $httpBackend.when('GET', /.*rest\/v1\/arvosanat\?suoritus=4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9$/).respond([{"id":"1f42f28e-96b7-446f-a109-41225a2e250d","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"GE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"GE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"25ac8ec4-60f3-4f93-8987-d55502057f94","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KO","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KO","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"8eef7eea-675e-4309-a741-3eb09b1c7169","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"cebbf0b7-abd2-4192-9676-b0bcedc67c71","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"HI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"HI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"9d14f993-78b0-4c0f-bb54-22131da55360","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"AI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"AI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"b0ccbecd-7aa3-4600-a0a2-1ebd8dfe7c58","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"LI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"LI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"7949b58c-d0a4-4177-ac9a-f6afb19fb0a5","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"YH","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"YH","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"dc54970c-9cd1-4e8f-8d97-a37af3e99c10","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"A1","lisatieto":"EN","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"A1","lisatieto":"EN","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"ceb92c4a-d449-43ea-8d38-562c6e488c05","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"KU","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"KU","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"64141c4e-a24f-4418-840e-034c9a509320","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"BI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"BI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"4610dd37-bb43-4855-87b3-a86e0b8da0bd","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KS","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KS","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"f0255ebb-f16f-447f-88a4-a0e6b75fd2d3","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"5","asteikko":"4-10"},"aine":"MU","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"5","asteikko":"4-10"},"aine":"MU","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"74b3ee94-b4c8-41b4-9ed3-0b44a38e8731","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KT","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KT","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"f1d0fe82-c30d-431b-9ef4-e0bb92d1c7d4","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"TE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"TE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"6a2ad1d6-7352-4346-9f21-b4e5ebb5bff0","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"FY","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"FY","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"cd60df16-7afe-4591-95d9-6804b0f7a9f6","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"MA","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"MA","valinnainen":false,"myonnetty":"04.06.2015"}}])
  $httpBackend.when('GET', /.*rest\/v1\/arvosanat\?suoritus=b3704e86-942f-43ed-b842-9d6570ecab4c$/).respond([{"id":"c8eec1d9-2287-4eb6-aa6c-099f787936d1","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"5","asteikko":"4-10"},"aine":"GE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"5","asteikko":"4-10"},"aine":"GE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"ac743331-220e-4d96-8b7a-9f585cd9396e","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KO","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KO","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"f22f0a01-eb98-47de-923e-21ada3b4e82b","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"KE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"KE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"6e531364-44f6-4bb8-a3d0-dfb78337b6e4","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"HI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"HI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"5098311b-c9b1-4bde-805a-7041234c5cc8","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"AI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"AI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"663a7428-39aa-4b4e-be8d-c472f7125628","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"LI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"LI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"25eda3e5-4dab-41fb-a80b-73d21adbadfa","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"YH","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"YH","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"702fd62c-d3ce-4a0c-a964-294c4696e8f9","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"A1","lisatieto":"EN","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"A1","lisatieto":"EN","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"23bdef95-8c6b-447e-b889-f8af04f619ac","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"KU","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"KU","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"634af76a-de6e-4bd6-bdb1-7295a4d4efac","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"BI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"BI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"4afdf8fa-5179-4f3f-9136-7214e0399699","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"KS","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"KS","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"1df5be9f-be5e-49ff-ae7c-2e81a303a87a","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"MU","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"MU","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"2490e720-4891-4964-80fb-1e8c4a1a51b0","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KT","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KT","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"ac1a67c2-999d-452c-9370-f8eab95a77ce","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"TE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"TE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"c7390ef1-85ec-45f5-8ab0-4c27b7053f43","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"FY","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"FY","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"c8640826-0fac-4bfd-9e98-8691b58e3bd6","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"MA","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"MA","valinnainen":false,"myonnetty":"04.06.2015"}}])

  $httpBackend.when('GET', /.*rest\/v1\/opiskeluoikeudet\?henkilo.*/).respond([])
  $httpBackend.when('GET', /.*rest\/v1\/komo/).respond({"yotutkintoKomoOid":"1.2.246.562.5.2013061010184237348007","perusopetusKomoOid":"1.2.246.562.13.62959769647","lisaopetusKomoOid":"1.2.246.562.5.2013112814572435044876","ammattistarttiKomoOid":"1.2.246.562.5.2013112814572438136372","valmentavaKomoOid":"1.2.246.562.5.2013112814572435755085","ammatilliseenvalmistavaKomoOid":"1.2.246.562.5.2013112814572441001730","ulkomainenkorvaavaKomoOid":"1.2.246.562.13.86722481404","lukioKomoOid":"TODO lukio komo oid","ammatillinenKomoOid":"TODO ammatillinen komo oid","lukioonvalmistavaKomoOid":"1.2.246.562.5.2013112814572429142840","ylioppilastutkintolautakunta":"1.2.246.562.10.43628088406"})

  $httpBackend.when('GET', /.*\/authentication-service\/buildversion\.txt\?auth/).respond("artifactId=authentication-service
                 version=13.0-MOCK
                 buildNumber=99
                 branchName=master
                 vcsRevision=xxx
                 buildTtime=20150227-0927")
  $httpBackend.when('GET', /.*\/cas\/myroles/).respond(["USER_robotti", "APP_HENKILONHALLINTA_CRUD", "APP_OID", "APP_HENKILONHALLINTA", "APP_ORGANISAATIOHALLINTA", "APP_KOODISTO_READ", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_READ", "APP_TARJONTA_CRUD", "APP_HENKILONHALLINTA_OPHREKISTERI", "APP_KOOSTEROOLIENHALLINTA_READ", "APP_KOODISTO", "APP_ANOMUSTENHALLINTA", "APP_OMATTIEDOT", "APP_TARJONTA", "APP_HAKUJENHALLINTA", "APP_OMATTIEDOT_READ_UPDATE", "APP_RAPORTOINTI", "APP_ANOMUSTENHALLINTA_CRUD", "APP_HAKUJENHALLINTA_CRUD", "VIRKAILIJA", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA", "APP_KOOSTEROOLIENHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD", "APP_OID_READ", "APP_HENKILONHALLINTA_READ", "APP_SIJOITTELU", "APP_HAKUJENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_LOKALISOINTI_CRUD_1.2.246.562.10.00000000001", "APP_YHTEYSTIETOTYYPPIENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEET_CRUD", "APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.00000000001", "APP_KOODISTO_READ_1.2.246.562.10.00000000001", "APP_OMATTIEDOT_CRUD_1.2.246.562.10.00000000001", "APP_TARJONTA_READ_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_CRUD", "APP_AITU_CRUD", "APP_VALINTOJENTOTEUTTAMINEN_CRUD_1.2.246.562.10.00000000001", "APP_ANOMUSTENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_LOKALISOINTI", "APP_SISALLONHALLINTA", "APP_TARJONTA_CRUD_1.2.246.562.10.00000000001", "APP_ORGANISAATIOHALLINTA_READ", "APP_TIEDONSIIRTO_CRUD_1.2.246.562.10.00000000001", "APP_OMATTIEDOT_CRUD", "APP_OID_CRUD", "APP_KOOSTEROOLIENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_KOODISTO_CRUD_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA_READ_1.2.246.562.10.00000000001", "APP_SISALLONHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_ANOMUSTENHALLINTA_READ", "APP_YHTEYSTIETOTYYPPIENHALLINTA_CRUD", "APP_KOODISTO_CRUD", "APP_AITU_CRUD_1.2.246.562.10.00000000001", "APP_ANOMUSTENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINEN", "APP_SUORITUSREKISTERI_CRUD", "APP_HAKEMUS_READ_UPDATE", "APP_HAKUJENHALLINTA_READ", "APP_RYHMASAHKOPOSTI_VIEW_1.2.246.562.10.00000000001", "APP_HAKULOMAKKEENHALLINTA", "APP_TIEDONSIIRTO_CRUD", "APP_SISALLONHALLINTA_CRUD", "APP_HAKEMUS", "APP_HAKEMUS_CRUD_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_HAKEMUS_CRUD", "APP_OSOITE_CRUD", "APP_YHTEYSTIETOTYYPPIENHALLINTA", "APP_OMATTIEDOT_READ_UPDATE_1.2.246.562.10.00000000001", "APP_ORGANISAATIOHALLINTA_READ_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_HAKULOMAKKEENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_OSOITE_CRUD_1.2.246.562.10.00000000001", "APP_RYHMASAHKOPOSTI", "APP_SIJOITTELU_CRUD_1.2.246.562.10.00000000001", "APP_LOKALISOINTI_CRUD", "APP_RYHMASAHKOPOSTI_SEND_1.2.246.562.10.00000000001", "APP_OID_READ_1.2.246.562.10.00000000001", "APP_HENKILONHALLINTA_OPHREKISTERI_1.2.246.562.10.00000000001", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_YHTEYSTIETOTYYPPIENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_KOOSTEROOLIENHALLINTA_CRUD", "APP_AITU", "APP_SUORITUSREKISTERI", "APP_TIEDONSIIRTO", "APP_YHTEYSTIETOTYYPPIENHALLINTA_READ", "APP_VALINTAPERUSTEET", "APP_TARJONTA_READ", "APP_VALINTOJENTOTEUTTAMINEN_CRUD", "APP_OID_CRUD_1.2.246.562.10.00000000001", "APP_HAKEMUS_READ_UPDATE_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEET_CRUD_1.2.246.562.10.00000000001", "APP_KOOSTEROOLIENHALLINTA_CRUD_1.2.246.562.10.00000000001", "APP_SIJOITTELU_CRUD", "APP_HAKUJENHALLINTA_READ_1.2.246.562.10.00000000001", "APP_RYHMASAHKOPOSTI_SEND", "APP_HAKULOMAKKEENHALLINTA_CRUD", "APP_RYHMASAHKOPOSTI_VIEW", "APP_OSOITE", "APP_HAKEMUS_LISATIETOCRUD_1.2.246.562.10.00000000001", "APP_ASIAKIRJAPALVELU_CREATE_TEMPLATE_1.2.246.562.10.00000000001", "APP_IPOSTI_SEND", "APP_VALINTAPERUSTEETKK_CRUD", "APP_ASIAKIRJAPALVELU_READ", "APP_ASIAKIRJAPALVELU_CREATE_TEMPLATE", "APP_IPOSTI_READ_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINENKK_CRUD_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINENKK_CRUD", "APP_ASIAKIRJAPALVELU_ASIOINTITILICRUD_1.2.246.562.10.00000000001", "APP_RAPORTOINTI_CRUD", "APP_IPOSTI_SEND_1.2.246.562.10.00000000001", "APP_KKHAKUVIRKAILIJA_CRUD_1.2.246.562.10.00000000001", "APP_IPOSTI", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_KK", "APP_IPOSTI_READ", "APP_VALINTOJENTOTEUTTAMINENKK", "APP_ASIAKIRJAPALVELU_CREATE_LETTER_1.2.246.562.10.00000000001", "APP_ASIAKIRJAPALVELU", "APP_KKHAKUVIRKAILIJA_CRUD", "APP_TARJONTA_KK_CRUD", "APP_ASIAKIRJAPALVELU_CREATE_LETTER", "APP_TARJONTA_KK_CRUD_1.2.246.562.10.00000000001", "APP_TARJONTA_KK", "APP_HAKEMUS_LISATIETOCRUD", "APP_VALINTAPERUSTEETKK_CRUD_1.2.246.562.10.00000000001", "APP_ASIAKIRJAPALVELU_ASIOINTITILICRUD", "APP_KKHAKUVIRKAILIJA", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_KK_CRUD", "APP_RAPORTOINTI_CRUD_1.2.246.562.10.00000000001", "APP_ASIAKIRJAPALVELU_READ_1.2.246.562.10.00000000001", "APP_VALINTAPERUSTEETKK", "APP_VALINTAPERUSTEKUVAUSTENHALLINTA_KK_CRUD_1.2.246.562.10.00000000001", "APP_ASIAKIRJAPALVELU_SEND_LETTER_EMAIL", "APP_ASIAKIRJAPALVELU_SEND_LETTER_EMAIL_1.2.246.562.10.00000000001", "APP_TIEDONSIIRTO_VALINTA_1.2.246.562.10.00000000001", "APP_TIEDONSIIRTO_VALINTA", "APP_VALINTOJENTOTEUTTAMINEN_TULOSTENTUONTI_1.2.246.562.10.00000000001", "APP_VALINTOJENTOTEUTTAMINEN_TULOSTENTUONTI", "APP_HAKULOMAKKEENHALLINTA_LOMAKEPOHJANVAIHTO", "APP_HAKULOMAKKEENHALLINTA_LOMAKEPOHJANVAIHTO_1.2.246.562.10.00000000001"])

