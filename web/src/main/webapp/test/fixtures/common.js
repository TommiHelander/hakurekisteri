var httpFixtures = function () {
    var httpBackend = testFrame().httpBackend
    var fixtures = {}

    fixtures.organisaatioService = {
        pikkarala: function() {
            httpBackend.when('GET', /.*\/organisaatio-service\/rest\/organisaatio\/v2\/hae\?aktiiviset=true&lakkautetut=false&organisaatiotyyppi=Oppilaitos&searchStr=Pik&suunnitellut=true$/).respond({"numHits":1,"organisaatiot":[{"oid":"1.2.246.562.10.39644336305","alkuPvm":694216800000,"parentOid":"1.2.246.562.10.80381044462","parentOidPath":"1.2.246.562.10.39644336305/1.2.246.562.10.80381044462/1.2.246.562.10.00000000001","oppilaitosKoodi":"06345","oppilaitostyyppi":"oppilaitostyyppi_11#1","match":true,"nimi":{"fi":"Pikkaralan ala-aste"},"kieletUris":["oppilaitoksenopetuskieli_1#1"],"kotipaikkaUri":"kunta_564","children":[],"organisaatiotyypit":["OPPILAITOS"],"aliOrganisaatioMaara":0}]})
        },
        pikkaralaKoodi: function () {
            httpBackend.when('GET', /.*\/organisaatio-service\/rest\/organisaatio\/06345$/).respond({"oid":"1.2.246.562.10.39644336305","nimi":{"fi":"Pikkaralan ala-aste"},"alkuPvm":"1992-01-01","postiosoite":{"osoiteTyyppi":"posti","yhteystietoOid":"1.2.246.562.5.75344290822","postinumeroUri":"posti_90310","osoite":"Vasantie 121","postitoimipaikka":"OULU","ytjPaivitysPvm":null,"lng":null,"lap":null,"coordinateType":null,"osavaltio":null,"extraRivi":null,"maaUri":null},"parentOid":"1.2.246.562.10.80381044462","parentOidPath":"|1.2.246.562.10.00000000001|1.2.246.562.10.80381044462|","vuosiluokat":[],"oppilaitosKoodi":"06345","kieletUris":["oppilaitoksenopetuskieli_1#1"],"oppilaitosTyyppiUri":"oppilaitostyyppi_11#1","yhteystiedot":[{"kieli":"kieli_fi#1","id":"22913","yhteystietoOid":"1.2.246.562.5.11296174961","email":"kaisa.tahtinen@ouka.fi"},{"tyyppi":"faksi","kieli":"kieli_fi#1","id":"22914","yhteystietoOid":"1.2.246.562.5.18105745956","numero":"08  5586 1582"},{"tyyppi":"puhelin","kieli":"kieli_fi#1","id":"22915","yhteystietoOid":"1.2.246.562.5.364178776310","numero":"08  5586 9514"},{"kieli":"kieli_fi#1","id":"22916","yhteystietoOid":"1.2.246.562.5.94533742915","www":"http://www.edu.ouka.fi/koulut/pikkarala"},{"osoiteTyyppi":"posti","kieli":"kieli_fi#1","id":"22917","yhteystietoOid":"1.2.246.562.5.75344290822","osoite":"Vasantie 121","postinumeroUri":"posti_90310","postitoimipaikka":"OULU","ytjPaivitysPvm":null,"coordinateType":null,"lap":null,"lng":null,"osavaltio":null,"extraRivi":null,"maaUri":null},{"osoiteTyyppi":"kaynti","kieli":"kieli_fi#1","id":"22918","yhteystietoOid":"1.2.246.562.5.58988409759","osoite":"Vasantie 121","postinumeroUri":"posti_90310","postitoimipaikka":"OULU","ytjPaivitysPvm":null,"coordinateType":null,"lap":null,"lng":null,"osavaltio":null,"extraRivi":null,"maaUri":null}],"kuvaus2":{},"tyypit":["Oppilaitos"],"yhteystietoArvos":[],"nimet":[{"nimi":{"fi":"Pikkaralan ala-aste"},"alkuPvm":"1992-01-01","version":1}],"ryhmatyypit":[],"kayttoryhmat":[],"kayntiosoite":{"osoiteTyyppi":"kaynti","yhteystietoOid":"1.2.246.562.5.58988409759","postinumeroUri":"posti_90310","osoite":"Vasantie 121","postitoimipaikka":"OULU","ytjPaivitysPvm":null,"lng":null,"lap":null,"coordinateType":null,"osavaltio":null,"extraRivi":null,"maaUri":null},"kotipaikkaUri":"kunta_564","maaUri":"maatjavaltiot1_fin","version":1,"status":"AKTIIVINEN"})
        },
        pikkaralaOid: function () {
            httpBackend.when('GET', /.*\/organisaatio-service\/rest\/organisaatio\/1\.2\.246\.562\.10\.39644336305$/).respond({"numHits":1,"organisaatiot":[{"oid":"1.2.246.562.10.39644336305","alkuPvm":694216800000,"parentOid":"1.2.246.562.10.80381044462","parentOidPath":"1.2.246.562.10.39644336305/1.2.246.562.10.80381044462/1.2.246.562.10.00000000001","oppilaitosKoodi":"06345","oppilaitostyyppi":"oppilaitostyyppi_11#1","match":true,"nimi":{"fi":"Pikkaralan ala-aste"},"kieletUris":["oppilaitoksenopetuskieli_1#1"],"kotipaikkaUri":"kunta_564","children":[],"aliOrganisaatioMaara":0,"organisaatiotyypit":["OPPILAITOS"]}]})
        }
    }

    fixtures.henkiloPalveluService = {
        aarne: function() {
            httpBackend.when('GET', /.*\/authentication-service\/resources\/henkilo\/1\.2\.246\.562\.24\.71944845619$/).respond({"id":90176,"etunimet":"Aarne","syntymaaika":"1958-10-12","passinnumero":null,"hetu":"123456-789","kutsumanimi":"aa","oidHenkilo":"1.2.246.562.24.71944845619","oppijanumero":null,"sukunimi":"AA","sukupuoli":"1","turvakielto":null,"henkiloTyyppi":"OPPIJA","eiSuomalaistaHetua":false,"passivoitu":false,"yksiloity":false,"yksiloityVTJ":true,"yksilointiYritetty":false,"duplicate":false,"created":null,"modified":null,"kasittelijaOid":null,"asiointiKieli":null,"aidinkieli":null,"kayttajatiedot":null,"kielisyys":[],"kansalaisuus":[]})
        },
        tyyne: function() {
            httpBackend.when('GET', /.*\/authentication-service\/resources\/henkilo\/1\.2\.246\.562\.24\.98743797763$/).respond({"id":90177,"etunimet":"Tyyne","syntymaaika":"1919-07-01","passinnumero":null,"hetu":"010719-917S","kutsumanimi":"aaa","oidHenkilo":"1.2.246.562.24.98743797763","oppijanumero":null,"sukunimi":"aaa","sukupuoli":"1","turvakielto":null,"henkiloTyyppi":"OPPIJA","eiSuomalaistaHetua":false,"passivoitu":false,"yksiloity":false,"yksiloityVTJ":true,"yksilointiYritetty":false,"duplicate":false,"created":null,"modified":null,"kasittelijaOid":null,"asiointiKieli":null,"aidinkieli":null,"huoltaja":null,"kayttajatiedot":null,"kielisyys":[],"kansalaisuus":[],"yhteystiedotRyhma":[]})
        },
        foobar: function() {
            httpBackend.when('GET', /.*\/authentication-service\/resources\/henkilo\?index=0&count=1&no=true&p=false&s=true&q=FOOBAR$/).respond({"totalCount":0,"results":[]})
        },
        aarneHenkiloPalvelu: function () {
            httpBackend.when('GET', /.*\/authentication-service\/resources\/henkilo\?index=0&count=1&no=true&p=false&s=true&q=1\.2\.246\.562\.24\.71944845619$/).respond({"totalCount":1,"results":[{"etunimet":"Aarne","syntymaaika":"1958-10-12","passinnumero":null,"hetu":"123456-789","kutsumanimi":"aa","oidHenkilo":"1.2.246.562.24.71944845619","oppijanumero":null,"sukunimi":"AA","sukupuoli":"1","turvakielto":null,"henkiloTyyppi":"OPPIJA","eiSuomalaistaHetua":false,"passivoitu":false,"yksiloity":false,"yksiloityVTJ":true,"yksilointiYritetty":false,"duplicate":false,"created":null,"modified":null,"kasittelijaOid":null,"asiointiKieli":null,"aidinkieli":null,"kayttajatiedot":null,"kielisyys":[],"kansalaisuus":[]}]})
        },
        aarneHenkiloPalveluHetu: function () {
            httpBackend.when('GET', /.*\/authentication-service\/resources\/henkilo\?index=0&count=1&no=true&p=false&s=true&q=123456-789$/).respond({"totalCount":1,"results":[{"etunimet":"Aarne","syntymaaika":"1958-10-12","passinnumero":null,"hetu":"123456-789","kutsumanimi":"aa","oidHenkilo":"1.2.246.562.24.71944845619","oppijanumero":null,"sukunimi":"AA","sukupuoli":"1","turvakielto":null,"henkiloTyyppi":"OPPIJA","eiSuomalaistaHetua":false,"passivoitu":false,"yksiloity":false,"yksiloityVTJ":true,"yksilointiYritetty":false,"duplicate":false,"created":null,"modified":null,"kasittelijaOid":null,"asiointiKieli":null,"aidinkieli":null,"kayttajatiedot":null,"kielisyys":[],"kansalaisuus":[]}]})
        },
        aarneHenkiloListana: function () {
            httpBackend.when('POST', /.*\/authentication-service\/resources\/henkilo\/henkilotByHenkiloOidList$/, ["1.2.246.562.24.71944845619"]).respond([{"etunimet":"Aarne","syntymaaika":"1958-10-12","passinnumero":null,"hetu":"123456-789","queryHetu":"123456-789","kutsumanimi":"aa","oidHenkilo":"1.2.246.562.24.71944845619","oppijanumero":null,"sukunimi":"AA","sukupuoli":"1","turvakielto":null,"henkiloTyyppi":"OPPIJA","eiSuomalaistaHetua":false,"passivoitu":false,"yksiloity":false,"yksiloityVTJ":true,"yksilointiYritetty":false,"duplicate":false,"created":null,"modified":null,"kasittelijaOid":null,"asiointiKieli":null,"aidinkieli":null,"kayttajatiedot":null,"kielisyys":[],"kansalaisuus":[]}])
        },
        aarneJaTyyneHenkiloListana: function () {
            httpBackend.when('POST', /.*\/authentication-service\/resources\/henkilo\/henkilotByHenkiloOidList$/, ["1.2.246.562.24.71944845619","1.2.246.562.24.49719248091","1.2.246.562.24.76359038731","1.2.246.562.24.87951154293","1.2.246.562.24.98743797763"]).respond([{"etunimet":"Aarne","syntymaaika":"1958-10-12","passinnumero":null,"hetu":"123456-789","kutsumanimi":"aa","oidHenkilo":"1.2.246.562.24.71944845619","oppijanumero":null,"sukunimi":"AA","sukupuoli":"1","turvakielto":null,"henkiloTyyppi":"OPPIJA","eiSuomalaistaHetua":false,"passivoitu":false,"yksiloity":false,"yksiloityVTJ":true,"yksilointiYritetty":false,"duplicate":false,"created":null,"modified":null,"kasittelijaOid":null,"asiointiKieli":null,"aidinkieli":null,"kayttajatiedot":null,"kielisyys":[],"kansalaisuus":[]},{"etunimet":"aaa","syntymaaika":"1900-07-09","passinnumero":null,"hetu":"090700-386W","kutsumanimi":"aaa","oidHenkilo":"1.2.246.562.24.49719248091","oppijanumero":null,"sukunimi":"aaa","sukupuoli":"2","turvakielto":null,"henkiloTyyppi":"OPPIJA","eiSuomalaistaHetua":false,"passivoitu":false,"yksiloity":false,"yksiloityVTJ":true,"yksilointiYritetty":false,"duplicate":false,"created":null,"modified":null,"kasittelijaOid":null,"asiointiKieli":null,"aidinkieli":null,"kayttajatiedot":null,"kielisyys":[],"kansalaisuus":[]},{"etunimet":"aaa","syntymaaika":"1998-03-06","passinnumero":null,"hetu":"060398-7570","kutsumanimi":"aaa","oidHenkilo":"1.2.246.562.24.76359038731","oppijanumero":null,"sukunimi":"aaa","sukupuoli":"1","turvakielto":null,"henkiloTyyppi":"OPPIJA","eiSuomalaistaHetua":false,"passivoitu":false,"yksiloity":false,"yksiloityVTJ":true,"yksilointiYritetty":false,"duplicate":false,"created":null,"modified":null,"kasittelijaOid":null,"asiointiKieli":null,"aidinkieli":null,"kayttajatiedot":null,"kielisyys":[],"kansalaisuus":[]},{"etunimet":"aaa","syntymaaika":"1920-04-26","passinnumero":null,"hetu":"260420-382F","kutsumanimi":"aaa","oidHenkilo":"1.2.246.562.24.87951154293","oppijanumero":null,"sukunimi":"aaa","sukupuoli":"2","turvakielto":null,"henkiloTyyppi":"OPPIJA","eiSuomalaistaHetua":false,"passivoitu":false,"yksiloity":false,"yksiloityVTJ":true,"yksilointiYritetty":false,"duplicate":false,"created":null,"modified":null,"kasittelijaOid":null,"asiointiKieli":null,"aidinkieli":null,"kayttajatiedot":null,"kielisyys":[],"kansalaisuus":[]},{"etunimet":"aaa","syntymaaika":"1919-07-01","passinnumero":null,"hetu":"010719-917S","kutsumanimi":"aaa","oidHenkilo":"1.2.246.562.24.98743797763","oppijanumero":null,"sukunimi":"aaa","sukupuoli":"1","turvakielto":null,"henkiloTyyppi":"OPPIJA","eiSuomalaistaHetua":false,"passivoitu":false,"yksiloity":false,"yksiloityVTJ":true,"yksilointiYritetty":false,"duplicate":false,"created":null,"modified":null,"kasittelijaOid":null,"asiointiKieli":null,"aidinkieli":null,"kayttajatiedot":null,"kielisyys":[],"kansalaisuus":[]}])
        }
    }

    fixtures.suorituksetLocal = {
        aarnenSuoritukset: function () {
            httpBackend.when('GET', /.*rest\/v1\/suoritukset\?henkilo=1.2.246.562.24.71944845619$/).respond([{"henkiloOid":"1.2.246.562.24.71944845619","source":"ophadmin","vahvistettu":true,"komo":"1.2.246.562.13.62959769647","myontaja":"1.2.246.562.10.39644336305","tila":"KESKEN","valmistuminen":"03.06.2015","yksilollistaminen":"Ei","suoritusKieli":"fi","id":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9"}])
        },
        tyynenSuoritukset: function () {
            httpBackend.when('GET', /.*rest\/v1\/suoritukset\?henkilo=1.2.246.562.24.98743797763$/).respond([{"henkiloOid":"1.2.246.562.24.98743797763","source":"Test","vahvistettu":true,"komo":"1.2.246.562.13.62959769647","myontaja":"1.2.246.562.10.39644336305","tila":"KESKEN","valmistuminen":"04.06.2015","yksilollistaminen":"Ei","suoritusKieli":"fi","id":"b3704e86-942f-43ed-b842-9d6570ecab4c"}])
        }
    }

    fixtures.arvosanatLocal = {
        aarnenArvosanat: function () {
            httpBackend.when('GET', /.*rest\/v1\/arvosanat\?suoritus=4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9$/).respond([{"id":"1f42f28e-96b7-446f-a109-41225a2e250d","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"GE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"GE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"25ac8ec4-60f3-4f93-8987-d55502057f94","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KO","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KO","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"8eef7eea-675e-4309-a741-3eb09b1c7169","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"cebbf0b7-abd2-4192-9676-b0bcedc67c71","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"HI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"HI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"9d14f993-78b0-4c0f-bb54-22131da55360","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"AI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"AI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"b0ccbecd-7aa3-4600-a0a2-1ebd8dfe7c58","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"LI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"LI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"7949b58c-d0a4-4177-ac9a-f6afb19fb0a5","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"YH","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"YH","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"dc54970c-9cd1-4e8f-8d97-a37af3e99c10","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"A1","lisatieto":"EN","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"A1","lisatieto":"EN","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"ceb92c4a-d449-43ea-8d38-562c6e488c05","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"KU","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"KU","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"64141c4e-a24f-4418-840e-034c9a509320","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"BI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"BI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"4610dd37-bb43-4855-87b3-a86e0b8da0bd","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KS","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KS","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"f0255ebb-f16f-447f-88a4-a0e6b75fd2d3","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"5","asteikko":"4-10"},"aine":"MU","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"5","asteikko":"4-10"},"aine":"MU","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"74b3ee94-b4c8-41b4-9ed3-0b44a38e8731","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KT","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KT","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"f1d0fe82-c30d-431b-9ef4-e0bb92d1c7d4","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"TE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"TE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"6a2ad1d6-7352-4346-9f21-b4e5ebb5bff0","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"FY","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"FY","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"cd60df16-7afe-4591-95d9-6804b0f7a9f6","suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"MA","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"4eed24c3-9569-4dd1-b7c7-8e0121f6a2b9","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"MA","valinnainen":false,"myonnetty":"04.06.2015"}}])
        },
        tyynenArvosanat: function () {
            httpBackend.when('GET', /.*rest\/v1\/arvosanat\?suoritus=b3704e86-942f-43ed-b842-9d6570ecab4c$/).respond([{"id":"c8eec1d9-2287-4eb6-aa6c-099f787936d1","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"5","asteikko":"4-10"},"aine":"GE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"5","asteikko":"4-10"},"aine":"GE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"ac743331-220e-4d96-8b7a-9f585cd9396e","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KO","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"KO","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"f22f0a01-eb98-47de-923e-21ada3b4e82b","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"KE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"KE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"6e531364-44f6-4bb8-a3d0-dfb78337b6e4","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"HI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"HI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"5098311b-c9b1-4bde-805a-7041234c5cc8","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"AI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"AI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"663a7428-39aa-4b4e-be8d-c472f7125628","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"LI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"LI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"25eda3e5-4dab-41fb-a80b-73d21adbadfa","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"YH","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"YH","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"702fd62c-d3ce-4a0c-a964-294c4696e8f9","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"A1","lisatieto":"EN","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"9","asteikko":"4-10"},"aine":"A1","lisatieto":"EN","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"23bdef95-8c6b-447e-b889-f8af04f619ac","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"KU","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"KU","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"634af76a-de6e-4bd6-bdb1-7295a4d4efac","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"BI","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"7","asteikko":"4-10"},"aine":"BI","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"4afdf8fa-5179-4f3f-9136-7214e0399699","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"KS","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"KS","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"1df5be9f-be5e-49ff-ae7c-2e81a303a87a","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"MU","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"MU","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"2490e720-4891-4964-80fb-1e8c4a1a51b0","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KT","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"KT","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"ac1a67c2-999d-452c-9370-f8eab95a77ce","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"TE","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"8","asteikko":"4-10"},"aine":"TE","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"c7390ef1-85ec-45f5-8ab0-4c27b7053f43","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"FY","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"6","asteikko":"4-10"},"aine":"FY","valinnainen":false,"myonnetty":"04.06.2015"}},{"id":"c8640826-0fac-4bfd-9e98-8691b58e3bd6","suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"MA","valinnainen":false,"myonnetty":"04.06.2015","source":"Test","core":{"suoritus":"b3704e86-942f-43ed-b842-9d6570ecab4c","arvio":{"arvosana":"10","asteikko":"4-10"},"aine":"MA","valinnainen":false,"myonnetty":"04.06.2015"}}])
        }
    }

    fixtures.luokkaTiedotLocal = {
        aarnenLuokkaTiedot: function () {
            httpBackend.when('GET', /.*rest\/v1\/opiskelijat\?henkilo=1.2.246.562.24.71944845619.*/).respond([{"id":"e86fb63a-607a-48da-b701-4527193e9efc","oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","luokka":"9A","henkiloOid":"1.2.246.562.24.71944845619","alkuPaiva":"2014-08-17T21:00:00.000Z","loppuPaiva":"2015-06-03T21:00:00.000Z","source":"Test","core":{"oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","henkiloOid":"1.2.246.562.24.71944845619"}}])
        },
        tyynenLuokkaTiedot: function () {
            httpBackend.when('GET', /.*rest\/v1\/opiskelijat\?henkilo=1.2.246.562.24.98743797763$/).respond([{"id":"67108ac8-11db-4ec1-b1a5-613ffd095251","oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","luokka":"9A","henkiloOid":"1.2.246.562.24.98743797763","alkuPaiva":"2014-08-17T21:00:00.000Z","loppuPaiva":"2015-06-03T21:00:00.000Z","source":"Test","core":{"oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","henkiloOid":"1.2.246.562.24.98743797763"}}])
        },
        tyynenLuokkaTiedotHetulla: function () {
            httpBackend.when('GET', /.*rest\/v1\/opiskelijat\?henkilo=123456-789&vuosi=2015$/).respond([{"id":"6812d1cb-bc15-435a-ab0c-53414d1a7775","oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","luokka":"9A","henkiloOid":"1.2.246.562.24.71944845619","alkuPaiva":"2014-08-17T21:00:00.000Z","loppuPaiva":"2015-06-03T21:00:00.000Z","source":"Test","core":{"oppilaitosOid":"1.2.246.562.10.39644336305","luokkataso":"9","henkiloOid":"1.2.246.562.24.71944845619"}}])
        }
    }

    fixtures.komoLocal = {
        komoTiedot: function() {
            httpBackend.when('GET', /.*rest\/v1\/komo/).respond({"yotutkintoKomoOid":"1.2.246.562.5.2013061010184237348007","perusopetusKomoOid":"1.2.246.562.13.62959769647","lisaopetusKomoOid":"1.2.246.562.5.2013112814572435044876","ammattistarttiKomoOid":"1.2.246.562.5.2013112814572438136372","valmentavaKomoOid":"1.2.246.562.5.2013112814572435755085","ammatilliseenvalmistavaKomoOid":"1.2.246.562.5.2013112814572441001730","ulkomainenkorvaavaKomoOid":"1.2.246.562.13.86722481404","lukioKomoOid":"TODO lukio komo oid","ammatillinenKomoOid":"TODO ammatillinen komo oid","lukioonvalmistavaKomoOid":"1.2.246.562.5.2013112814572429142840","ylioppilastutkintolautakunta":"1.2.246.562.10.43628088406"})
        }
    }

    return fixtures
}