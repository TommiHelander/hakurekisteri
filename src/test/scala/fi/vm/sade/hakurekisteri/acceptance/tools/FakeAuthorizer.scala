package fi.vm.sade.hakurekisteri.acceptance.tools

import akka.actor.{Actor, ActorRef}
import fi.vm.sade.hakurekisteri.organization.{AuthorizedCreate, AuthorizedQuery, AuthorizedRead, AuthorizedUpdate}


class FakeAuthorizer(guarded:ActorRef) extends Actor {
  override def receive: FakeAuthorizer#Receive =  {
    case AuthorizedQuery(q,_) => guarded forward q
    case AuthorizedRead(id, _) => guarded forward id
    case AuthorizedCreate(resource, _ , _) => guarded forward resource
    case AuthorizedUpdate(resource, _ , _) => guarded forward resource
    case message:AnyRef => guarded forward message
  }
}
