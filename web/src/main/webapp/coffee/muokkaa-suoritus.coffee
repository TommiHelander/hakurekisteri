app.controller "MuokkaaSuoritus", [
  "$scope"
  "$http"
  "$q"
  "MessageService"
  ($scope, $http, $q, MessageService) ->
    enrichSuoritus = (suoritus) ->
      if suoritus.myontaja
        getOrganisaatio $http, suoritus.myontaja, (organisaatio) ->
          $scope.info.oppilaitos = organisaatio.oppilaitosKoodi
          $scope.info.organisaatio = organisaatio
      if suoritus.komo and suoritus.komo.match(/^koulutus_\d*$/)
        getKoulutusNimi $http, suoritus.komo, (koulutusNimi) ->
          $scope.info.koulutus = koulutusNimi
      else
        $scope.info.editable = true

    $scope.validateData = ->
      $scope.validateOppilaitoskoodiFromScopeAndUpdateMyontajaInModel($scope.info, $scope.suoritus)

    $scope.hasChanged = ->
      $scope.suoritus.valmistuminen = formatDateWithZeroPaddedNumbers($scope.info.valmistuminen)
      modifiedCache.hasChanged()

    $scope.saveData = ->
      if $scope.hasChanged()
        d = $q.defer()
        suoritus = $scope.suoritus
        if $scope.info.delete
          if suoritus.id
            suoritus.$remove (->
              deleteFromArray suoritus, $scope.henkilo.suoritukset
              d.resolve "done"
            ), ->
              MessageService.addMessage
                type: "danger"
                messageKey: "suoritusrekisteri.muokkaa.virhetallennettaessasuoritustietoja"
                message: "Virhe tallennettaessa suoritustietoja."
                descriptionKey: "suoritusrekisteri.muokkaa.virhesuoritusyrita"
                description: "Yritä uudelleen."
              d.reject "error deleting suoritus: " + suoritus
          else
            deleteFromArray suoritus, $scope.henkilo.suoritukset
            d.resolve "done"
        else
          suoritus.$save (->
            enrichSuoritus suoritus
            d.resolve "done"
          ), ->
            MessageService.addMessage
              type: "danger"
              messageKey: "suoritusrekisteri.muokkaa.virhetallennettaessasuoritustietoja"
              message: "Virhe tallennettaessa suoritustietoja."
              descriptionKey: "suoritusrekisteri.muokkaa.virhesuoritusyrita"
              description: "Yritä uudelleen."
            d.reject "error saving suoritus: " + suoritus
        d.promise.then ->
          modifiedCache.update()
        [d.promise]
      else
        []

    $scope.parseFinDate = (input) ->
      parts = input.split('.')
      new Date(parts[2], parts[1]-1, parts[0])

    pad = (n) ->
      if n<10
        '0'+n
      else
        n

    formatDateNoZeroPaddedNumbers = (input) ->
      date = $scope.parseFinDate(input)
      ""+date.getDate()+"."+(date.getMonth()+1)+"."+date.getFullYear()

    formatDateWithZeroPaddedNumbers = (date) ->
      if typeof date is 'string'
        date = $scope.parseFinDate(date)
      "" + pad(date.getDate()) + "." + pad(date.getMonth()+1) + "." + date.getFullYear()

    modifiedCache = changeDetection($scope.suoritus)
    $scope.info = {}
    $scope.info.valmistuminen = formatDateNoZeroPaddedNumbers($scope.suoritus.valmistuminen)
    enrichSuoritus($scope.suoritus)
    $scope.addDataScope($scope)
    $scope.$watch "info.valmistuminen", $scope.enableSave, true
    $scope.$watch "suoritus", $scope.enableSave, true
]
