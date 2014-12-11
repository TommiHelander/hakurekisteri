"use strict"

app = angular.module "myApp", [
  "ngRoute"
  "ngResource"
  "ui.bootstrap"
  "ngUpload"
  "ngSanitize"
]

app.factory "Opiskelijat", ($resource) ->
  $resource "rest/v1/opiskelijat/:opiskelijaId", { opiskelijaId: "@id" }, {
      query:
        method: "GET"
        isArray: true
        cache: false
        timeout: 55000

      save:
        method: "POST"
        timeout: 15000

      remove:
        method: "DELETE"
        timeout: 15000
  }


app.factory "Suoritukset", ($resource) ->
  $resource "rest/v1/suoritukset/:suoritusId", { suoritusId: "@id" }, {
    query:
      method: "GET"
      isArray: true
      cache: false
      timeout: 55000
    save:
      method: "POST"
      timeout: 15000
    remove:
      method: "DELETE"
      timeout: 15000
  }


app.factory "Opiskeluoikeudet", ($resource) ->
  $resource "rest/v1/opiskeluoikeudet/:opiskeluoikeusId", { opiskeluoikeusId: "@id" }, {
    query:
      method: "GET"
      isArray: true
      cache: false
      timeout: 55000

    save:
      method: "POST"
      timeout: 15000

    remove:
      method: "DELETE"
      timeout: 15000
  }

app.factory "Arvosanat", ($resource) ->
  $resource "rest/v1/arvosanat/:arvosanaId", { arvosanaId: "@id" }, {
    query:
      method: "GET"
      isArray: true
      cache: false
      timeout: 55000

    save:
      method: "POST"
      timeout: 30000

    remove:
      method: "DELETE"
      timeout: 15000
  }


app.factory "MurupolkuService", ->
  murupolku = []
  hide = false

  return (
    murupolku: murupolku
    addToMurupolku: (item, reset) ->
      murupolku.length = 0  if reset
      murupolku.push item
      hide = false
      return
    hideMurupolku: ->
      hide = true
      return
    isHidden: ->
      hide
  )


app.factory "MessageService", ->
  messages = []
  return (
    messages: messages
    addMessage: (message, clear) ->
      messages.length = 0  if clear
      messages.push message
      return

    removeMessage: (message) ->
      index = messages.indexOf(message)
      messages.splice index, 1  if index isnt -1
      return

    clearMessages: ->
      messages.length = 0
      return
  )

app.filter "hilight", ->
  (input, query) ->
    input.replace new RegExp("(" + query + ")", "gi"), "<strong>$1</strong>"

app.directive "messages", ->
  return (
    controller: ($scope, MessageService) ->
      $scope.messages = MessageService.messages
      $scope.removeMessage = MessageService.removeMessage
      return

    templateUrl: "templates/messages"
  )

app.directive "tiedonsiirtomenu", ->
  return (
    controller: ($scope, $location) ->
      $scope.isActive = (path) ->
        path is $location.path()

      return

    templateUrl: "templates/tiedonsiirtomenu"
  )
