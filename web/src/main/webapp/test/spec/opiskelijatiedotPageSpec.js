(function () {
    function asyncPrint(s) {
        return function () {
            console.log(s)
        }
    }

    describe('Opiskelijatiedot', function () {
        var page = opiskelijatiedotPage()

        beforeEach(function (done) {
            page.openPage(done)
        })

        afterEach(function () {
            if (this.currentTest.state == 'failed') {
                takeScreenshot()
            }
        })

        describe("Haku", function () {
            it('Voi hakea oppilaitoksen perusteella - test-dsl', seqDone(
                wait.forAngular,
                function() {
                    httpFixtures().organisaatioService.pikkarala()
                    httpFixtures().organisaatioService.pikkaralaKoodi()
                    httpFixtures().organisaatioService.pikkaralaOid()
                    httpFixtures().henkiloPalveluService.aarne()
                    httpFixtures().henkiloPalveluService.aarneHenkiloPalvelu()
                    httpFixtures().henkiloPalveluService.aarneJaTyyneHenkiloListana()
                    httpFixtures().suorituksetLocal.aarnenSuoritukset()
                    httpFixtures().henkiloPalveluService.aarneJaTyyneHenkiloListana()
                    httpFixtures().arvosanatLocal.aarnenArvosanat()
                    httpFixtures().luokkaTiedotLocal.aarnenLuokkaTiedot()
                    httpFixtures().komoLocal.komoTiedot()
                    httpFixtures().rekisteriTiedotLocal.rekisteriTiedot()
                    koodistoFixtures()
                },
                autocomplete(opiskelijatiedot.organizationSearch, "Pik", opiskelijatiedot.organizationDropDownMenuChild(1)),
                wait.forAngular,
                click(opiskelijatiedot.searchButton),
                wait.forAngular,
                function () {
                    expect(opiskelijatiedot.resultsTable().length).to.equal(5)
                }
            ))
        })

        describe('Henkilohaku', function () {
            it('Voi hakea oidin perusteella - test-dsl', seqDone(
                wait.forAngular,
                function() {
                    httpFixtures().organisaatioService.pikkaralaOid()
                    httpFixtures().henkiloPalveluService.aarne()
                    httpFixtures().henkiloPalveluService.aarneHenkiloPalvelu()
                    httpFixtures().henkiloPalveluService.aarneHenkiloListana()
                    httpFixtures().arvosanatLocal.aarnenArvosanat()
                    httpFixtures().suorituksetLocal.aarnenSuoritukset()
                    httpFixtures().luokkaTiedotLocal.aarnenLuokkaTiedot()
                    httpFixtures().komoLocal.komoTiedot()
                    koodistoFixtures()
                },
                input(opiskelijatiedot.henkiloSearch, '1.2.246.562.24.71944845619'),
                click(opiskelijatiedot.searchButton),
                wait.forAngular,
                function () {
                    expect(opiskelijatiedot.resultsTable().length).to.equal(1)
                    expect(opiskelijatiedot.henkiloTiedot().is(':visible')).to.equal(true)
                    expect(opiskelijatiedot.suoritusTiedot().is(':visible')).to.equal(true)
                    expect(opiskelijatiedot.luokkaTiedot().is(':visible')).to.equal(true)
                }
            ))

            it('Voi hakea hetun perusteella - test.dsl', seqDone(
                wait.forAngular,
                function() {
                    httpFixtures().organisaatioService.pikkaralaOid()
                    httpFixtures().henkiloPalveluService.aarne()
                    httpFixtures().henkiloPalveluService.aarneHenkiloPalveluHetu()
                    httpFixtures().henkiloPalveluService.aarneHenkiloListana()
                    httpFixtures().suorituksetLocal.aarnenSuoritukset()
                    httpFixtures().arvosanatLocal.aarnenArvosanat()
                    httpFixtures().luokkaTiedotLocal.aarnenLuokkaTiedot()
                    httpFixtures().luokkaTiedotLocal.tyynenLuokkaTiedotHetulla()
                    httpFixtures().komoLocal.komoTiedot()
                    koodistoFixtures()
                },
                input(opiskelijatiedot.henkiloSearch, '123456-789'),
                click(opiskelijatiedot.searchButton),
                wait.forAngular,
                function () {
                    expect(opiskelijatiedot.resultsTable().length).to.equal(1)
                    expect(opiskelijatiedot.henkiloTiedot().is(':visible')).to.equal(true)
                    expect(opiskelijatiedot.suoritusTiedot().is(':visible')).to.equal(true)
                    expect(opiskelijatiedot.luokkaTiedot().is(':visible')).to.equal(true)
                }
            ))

            it('Puuttuva hetu perusteella - test.dsl', seqDone(
                wait.forAngular,
                function() {
                    httpFixtures().organisaatioService.pikkaralaOid()
                    httpFixtures().henkiloPalveluService.foobar()
                    httpFixtures().arvosanatLocal.aarnenArvosanat()
                    httpFixtures().suorituksetLocal.aarnenSuoritukset()
                    httpFixtures().luokkaTiedotLocal.aarnenLuokkaTiedot()
                },
                input(opiskelijatiedot.henkiloSearch, 'foobar'),
                click(opiskelijatiedot.searchButton),
                wait.forAngular,
                function () {
                    expect(opiskelijatiedot.resultsTable().length).to.equal(0)
                    expect(opiskelijatiedot.henkiloTiedot().is(':visible')).to.equal(false)
                    expect(opiskelijatiedot.suoritusTiedot().is(':visible')).to.equal(false)
                    expect(opiskelijatiedot.luokkaTiedot().is(':visible')).to.equal(false)
                }
            ))
        })

        describe('Oppilaiden valitseminen/etsiminen organisaatiossa', function () {

            function assertText(selector, val) {
                expect(selector().text().trim()).to.equal(val)
            }

            function areElementsVisible() {
                return opiskelijatiedot.henkiloTiedot().is(':visible') &&
                    opiskelijatiedot.suoritusTiedot().is(':visible') &&
                    opiskelijatiedot.luokkaTiedot().is(':visible')
            }

            it('Etsi organisaatiosta henkiloita - test.dsl', seqDone(
                wait.forAngular,
                function() {
                    httpFixtures().organisaatioService.pikkarala()
                    httpFixtures().organisaatioService.pikkaralaKoodi()
                    httpFixtures().organisaatioService.pikkaralaOid()
                    httpFixtures().henkiloPalveluService.aarne()
                    httpFixtures().henkiloPalveluService.tyyne()
                    httpFixtures().henkiloPalveluService.aarneHenkiloPalveluHetu()
                    httpFixtures().henkiloPalveluService.aarneJaTyyneHenkiloListana()
                    httpFixtures().suorituksetLocal.aarnenSuoritukset()
                    httpFixtures().suorituksetLocal.tyynenSuoritukset()
                    httpFixtures().arvosanatLocal.aarnenArvosanat()
                    httpFixtures().arvosanatLocal.tyynenArvosanat()
                    httpFixtures().luokkaTiedotLocal.aarnenLuokkaTiedot()
                    httpFixtures().luokkaTiedotLocal.tyynenLuokkaTiedot()
                    httpFixtures().komoLocal.komoTiedot()
                    httpFixtures().rekisteriTiedotLocal.rekisteriTiedot()
                    koodistoFixtures()
                },
                autocomplete(opiskelijatiedot.organizationSearch, "Pik", opiskelijatiedot.organizationDropDownMenuChild(1)),
                wait.forAngular,
                click(opiskelijatiedot.searchButton),
                wait.forAngular,
                click(opiskelijatiedot.resultsTableChild(1)),
                wait.forAngular,
                function () {
                    expect(areElementsVisible()).to.equal(true)
                    assertText(opiskelijatiedot.hetuTieto, "123456-789")
                },
                click(opiskelijatiedot.resultsTableChild(2)),
                wait.forAngular,
                function () {
                    expect(areElementsVisible()).to.equal(true)
                    assertText(opiskelijatiedot.hetuTieto, "010719-917S")
                }
            ))
        })
    })
})()