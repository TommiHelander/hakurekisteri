'use strict';

var msgCategory = "suoritusrekisteri";

function OpiskelijatCtrl($scope, $routeParams, $log, $http, Henkilo, HenkiloByHetu, Organisaatio, Opiskelijat) {
    $scope.loading = false;
    $scope.currentRows = [];
    $scope.allRows = [];
    $scope.sorting = { field: "", direction: "desc" };
    $scope.pageNumbers = [];
    $scope.page = 0;
    $scope.pageSize = 10;
    $scope.filter = {star: $routeParams.star ? $routeParams.star : ""};
    $scope.targetOrg = "";
    $scope.myRoles = [];

    function getMyRoles() {
        $http.get('/cas/myroles')
            .success(function(data) {
                $scope.myRoles = angular.fromJson(data);
            })
            .error(function() {
                if (location.hostname === 'localhost') {
                    $scope.myRoles = ["APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.00000000001"];
                }
                $log.error("cannot connect to CAS");
            });
    }
    getMyRoles();
    $scope.isOPH = function() {
        return (Array.isArray($scope.myRoles)
                && ($scope.myRoles.indexOf("APP_SUORITUSREKISTERI_CRUD_1.2.246.562.10.00000000001") > -1
                        || $scope.myRoles.indexOf("APP_SUORITUSREKISTERI_READ_UPDATE_1.2.246.562.10.00000000001") > -1));
    };

    $scope.fetch = function() {
        $scope.currentRows = [];
        $scope.allRows = [];
        $scope.loading = true;
        $scope.hakuehto = "";

        if ($scope.searchTerm && $scope.searchTerm.match(/^\d{6}[+-AB]\d{3}[0-9a-zA-Z]$/)) {
            HenkiloByHetu.getCached({hetu: $scope.searchTerm}, function(henkilo) {
                $scope.hakuehto = henkilo.hetu + ' (' + henkilo.etunimet + ' ' + henkilo.sukunimi + ')';
                search({henkiloOid: henkilo.oidHenkilo});
            }, function() {
                $scope.hakuehto = $scope.searchTerm;
                $scope.loading = false;
            });
        } else if ($scope.searchTerm && $scope.searchTerm.match(/^\d{5}$/)) {
            Organisaatio.getCached({organisaatioOid: $scope.searchTerm}, function(organisaatio) {
                $scope.hakuehto = organisaatio.oppilaitosKoodi + ' (' + (organisaatio.nimi.fi ? organisaatio.nimi.fi : (organisaatio.nimi.sv ? organisaatio.nimi.sv : 'Oppilaitoksen nimeä ei löytynyt')) + ')';
                search({oppilaitosOid: organisaatio.oid});
            }, function() {
                $scope.hakuehto = $scope.searchTerm;
                $scope.loading = false;
            });
        } else {
            search({});
        }
        function search(query) {
            Opiskelijat.get(query, function(opiskelijat) {
                if (Array.isArray(opiskelijat)) {
                    showCurrentRows(opiskelijat);
                }
                resetPageNumbers();
                $scope.loading = false;
            }, function() {
                $scope.loading = false;
            });
        }
    };

    function showCurrentRows(allRows) {
        $scope.allRows = allRows;
        $scope.currentRows = allRows.slice($scope.page * $scope.pageSize, ($scope.page + 1) * $scope.pageSize);
        enrichData();
    }

    function enrichData() {
        for (var i = 0; i < $scope.currentRows.length; i++) {
            var opiskelija = $scope.currentRows[i];
            if (opiskelija.oppilaitosOid) {
                Organisaatio.getCached({organisaatioOid: opiskelija.oppilaitosOid}, function(data) {
                    if (data && data.oid === opiskelija.oppilaitosOid)
                        opiskelija.oppilaitos = data.oppilaitosKoodi + ' ' + data.nimi.fi;
                });
            }
            if (opiskelija.henkiloOid) {
                Henkilo.getCached({henkiloOid: opiskelija.henkiloOid}, function(henkilo) {
                    if (henkilo && henkilo.oidHenkilo === opiskelija.henkiloOid && henkilo.sukunimi && henkilo.etunimet) {
                        opiskelija.henkilo = henkilo.sukunimi + ", " + henkilo.etunimet + (henkilo.hetu ? " (" + henkilo.hetu + ")" : "");
                    }
                });
            }
        }
    }

    $scope.nextPage = function() {
        if (($scope.page + 1) * $scope.pageSize < $scope.allRows.length) {
            $scope.page++;
        } else {
            $scope.page = 0;
        }
        showCurrentRows($scope.allRows);
    };
    $scope.prevPage = function() {
        if ($scope.page > 0 && ($scope.page - 1) * $scope.pageSize < $scope.allRows.length) {
            $scope.page--;
        } else {
            $scope.page = Math.floor($scope.allRows.length / $scope.pageSize);
        }
        showCurrentRows($scope.allRows);
    };
    $scope.showPageWithNumber = function(pageNum) {
        $scope.page = pageNum > 0 ? (pageNum - 1) : 0;
        showCurrentRows($scope.allRows);
    };
    $scope.setPageSize = function(newSize) {
        $scope.pageSize = newSize;
        $scope.page = 0;
        resetPageNumbers();
        showCurrentRows($scope.allRows);
    };
    $scope.sort = function(field, direction) {
        $scope.sorting.field = field;
        $scope.sorting.direction = direction.match(/asc|desc/) ? direction : 'asc';
        $scope.page = 0;
        showCurrentRows($scope.allRows);
    };
    $scope.isDirectionIconVisible = function(field) {
        return $scope.sorting.field === field;
    };

    function resetPageNumbers() {
        $scope.pageNumbers = [];
        for (var i = 0; i < Math.ceil($scope.allRows.length / $scope.pageSize); i++) {
            $scope.pageNumbers.push(i + 1);
        }
    }

    $scope.fetch();
}

function getKoodi(koodiArray, koodiArvo) {
    for (var i = 0; i < koodiArray.length; i++) {
        var koodi = koodiArray[i];
        if (koodi.koodiArvo == koodiArvo) {
            for (var m = 0; m < koodi.metadata.length; m++) {
                var metadata = koodi.metadata[m];
                if (metadata.kieli == "FI") {
                    return metadata.nimi;
                }
            }
        }
    }
    return koodiArvo;
}


function MuokkaaCtrl($scope, $routeParams, $location, Henkilo, Organisaatio, Koodi, Koodisto, Opiskelijat, Suoritukset) {
    $scope.errors = [];
    $scope.henkiloOid = $routeParams.henkiloOid;

    function fetchHenkilotiedot() {
        Henkilo.get({henkiloOid: $scope.henkiloOid}, function(henkilo) {
            $scope.henkilo = henkilo;
        }, function() {
            confirm("Henkilötietojen hakeminen epäonnistui. Yritä uudelleen?") ? fetchHenkilotiedot() : $location.path("/opiskelijat");
        });
    }
    fetchHenkilotiedot();

    function fetchOpiskelijatiedot() {
        Opiskelijat.get({henkiloOid: $scope.henkiloOid}, function(luokkatiedot) {
            $scope.luokkatiedot = luokkatiedot;
        }, function() {
            confirm("Luokkatietojen hakeminen epäonnistui. Yritä uudelleen?") ? fetchOpiskelijatiedot() : $location.path("/opiskelijat");
        });
    }
    fetchOpiskelijatiedot();

    function fetchSuoritukset() {
        Suoritukset.get({henkiloOid: $scope.henkiloOid}, function(suoritukset) {
            $scope.suoritukset = suoritukset;
            enrichSuoritukset();
        }, function() {
            confirm("Suoritustietojen hakeminen epäonnistui. Yritä uudelleen?") ? fetchSuoritukset() : $location.path("/opiskelijat");
        });
    }
    fetchSuoritukset();

    function enrichSuoritukset() {
        if ($scope.suoritukset) {
            for (var i = 0; i < $scope.suoritukset.length; i++) {
                var suoritus = $scope.suoritukset[i];
                if (suoritus.komoto && suoritus.komoto.tarjoaja) {
                    Organisaatio.getCached({organisaatioOid: suoritus.komoto.tarjoaja}, function(organisaatio) {
                        if (organisaatio.oid === suoritus.komoto.tarjoaja) {
                            suoritus.oppilaitos = organisaatio.nimi.fi ? organisaatio.nimi.fi : organisaatio.nimi.sv;
                        }
                    });
                }
            }
        }
    }

    $scope.yksilollistamiset = ["Ei", "Osittain", "Kokonaan", "Alueittain"];

    getKoodistoAsOptionArray("maatjavaltiot2", Koodisto, 'FI', $scope.maat);
    getKoodistoAsOptionArray("kunta", Koodisto, 'FI', $scope.kunnat);
    getKoodistoAsOptionArray("kieli", Koodisto, 'FI', $scope.kielet);
    getKoodistoAsOptionArray("maatjavaltiot2", Koodisto, 'FI', $scope.kansalaisuudet);

    $scope.fetchPostitoimipaikka = function() {
        if ($scope.henkilo.postinumero && $scope.henkilo.postinumero.match(/^\d{5}$/)) {
            $scope.searchingPostinumero = true;
            Koodi.getCached({koodisto: "posti", koodiUri: "posti_" + $scope.henkilo.postinumero}, function(koodi) {
                for (var i = 0; i < koodi.metadata.length; i++) {
                    var meta = koodi.metadata[i];
                    if (meta.kieli === 'FI') {
                        $scope.henkilo.postitoimipaikka = meta.nimi;
                        break;
                    }
                }
                $scope.searchingPostinumero = false;
            }, function() {
                $scope.henkilo.postitoimipaikka = "Postitoimipaikkaa ei löytynyt";
                $scope.searchingPostinumero = false;
            });
        }
    };

    // tallennus
    $scope.save = function() {
        $scope.errors.push({
            message: "Tallennusta ei vielä toteutettu.",
            description: ""
        });
    };
    $scope.cancel = function() {
        $location.path("/opiskelijat")
    };
    $scope.removeError = function(error) {
        var index = $scope.errors.indexOf(error);
        if (index !== -1) {
            $scope.errors.splice(index, 1);
        }
    };

    // datepicker
    $scope.showWeeks = false;
    $scope.pickDate = function($event, openedKey) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope[openedKey] = true;
    };
    $scope.dateOptions = {
        'year-format': "'yyyy'",
        'starting-day': 1
    };
    $scope.format = 'dd.MM.yyyy';
}

function getKoodistoAsOptionArray(koodisto, Koodisto, kielikoodi, options) {
    options = [];
    Koodisto.getCached({koodisto: koodisto}, function(koodisto) {
        for (var i = 0; i < koodisto.length; i++) {
            var koodi = koodisto[i];
            metas: for (var j = 0; j < koodi.metadata.length; j++) {
                var meta = koodi.metadata[j];
                if (meta.kieli.toLowerCase() === kielikoodi.toLowerCase()) {
                    options.push({
                        value: koodi.koodiArvo,
                        text: meta.nimi
                    });
                    break metas;
                }
            }
        }
        options.sort(function(a, b) {
            if (a.text === b.text) return 0;
            return a.text < b.text ? -1 : 1;
        });
    });
}
