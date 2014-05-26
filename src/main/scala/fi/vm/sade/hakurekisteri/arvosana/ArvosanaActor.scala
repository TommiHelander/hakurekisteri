package fi.vm.sade.hakurekisteri.arvosana

import fi.vm.sade.hakurekisteri.rest.support.Query
import fi.vm.sade.hakurekisteri.storage._
import fi.vm.sade.hakurekisteri.storage.repository._
import scala.Some
import java.util.UUID
import fi.vm.sade.hakurekisteri.suoritus.{SuoritusQuery, Suoritus}
import scala.concurrent.Future


trait ArvosanaRepository extends JournaledRepository[Arvosana] {

  var suoritusIndex: Map[UUID, Seq[Arvosana with Identified]] = Map()

  for (arvosana <- store.values) addNew(arvosana)


  def addNew(arvosana: Arvosana with Identified) = {
    suoritusIndex = suoritusIndex + (arvosana.suoritus -> suoritusIndex.get(arvosana.suoritus).getOrElse(Seq()))


  }


  override def index(old: Option[Arvosana with Identified], current: Arvosana with Identified) {

    def removeOld(arvosana: Arvosana with Identified) = {
      suoritusIndex = suoritusIndex.get(arvosana.suoritus).
        map(_.filter(_ != arvosana)).
        map((ns) => suoritusIndex + (arvosana.suoritus -> ns)).getOrElse(suoritusIndex)

    }

    old.foreach((s) => removeOld(s))
    addNew(current)

  }

    def identify(o:Arvosana): Arvosana with Identified = Arvosana.identify(o)
}

trait ArvosanaService extends ResourceService[Arvosana]  with ArvosanaRepository {


  override val optimize:PartialFunction[Query[Arvosana], Future[Seq[Arvosana with Identified]]] = {
    case ArvosanaQuery(Some(suoritus)) => Future.successful(suoritusIndex.get(suoritus).getOrElse(Seq()))

  }


  override val matcher: PartialFunction[Query[Arvosana], (Arvosana with Identified) => Boolean] = {
    case ArvosanaQuery(None) => (a) => true
    case ArvosanaQuery(Some(suoritus)) => (a) => a.suoritus == suoritus
  }
}

class ArvosanaActor(val journal:Journal[Arvosana] = new InMemJournal[Arvosana]) extends ResourceActor[Arvosana] with ArvosanaRepository with ArvosanaService {
}





