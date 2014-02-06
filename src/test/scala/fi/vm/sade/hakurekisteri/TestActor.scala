package fi.vm.sade.hakurekisteri

import fi.vm.sade.hakurekisteri.storage.{Identified, ResourceService, Repository, ResourceActor}
import fi.vm.sade.hakurekisteri.rest.support.Query
import java.util.UUID


class TestActor extends ResourceActor[Resource]  with Repository[Resource] with ResourceService[Resource] {

  var store:Seq[Resource] = Seq()


  def save(t: Resource): Resource with Identified = {
    println("saving: " + t)
    store = t +: store
    identify(t)
  }


  def identify(t: Resource): Resource with Identified {val id: UUID} = {
    new Resource(t.name) with Identified {
      val id = UUID.randomUUID()
    }
  }

  def listAll(): Seq[Resource with Identified] = store.map(identify)

  val matcher: PartialFunction[Query[Resource], (Resource with Identified) => Boolean] = { case _ => (_) => true}

}
