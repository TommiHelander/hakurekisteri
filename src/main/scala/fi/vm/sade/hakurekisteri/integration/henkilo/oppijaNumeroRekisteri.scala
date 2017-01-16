package fi.vm.sade.hakurekisteri.integration.henkilo

import akka.actor.ActorSystem
import akka.event.Logging
import fi.vm.sade.hakurekisteri.integration.VirkailijaRestClient
import org.apache.commons.httpclient.HttpStatus

import scala.collection.Iterator
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait IOppijaNumeroRekisteri {
  /**
    Fetches linked henkilo oids from oppijanumerorekisteri.
    Return map where every oid is mathed to set of linked oids

    Example: Henkilos A and B are linked. C is not linked. Therefore this method returns map:
    A -> [A, B]
    B -> [A, B]
    C -> [C]
    */
  def fetchLinkedHenkiloOidsMap(henkiloOids: Set[String]): Future[Map[String, Set[String]]]

  def enrichWithAliases(henkiloOids: Set[String]): Future[PersonOidsWithAliases] = {
    fetchLinkedHenkiloOidsMap(henkiloOids).map(PersonOidsWithAliases(henkiloOids, _))
  }
}

object IOppijaNumeroRekisteri {
  /**
    Appends linked henkilo oids to henkiloOids Set.
   */
  def combineLinkedHenkiloOids(henkiloOids: Set[String], links: Map[String, Set[String]]): Set[String] = {
    henkiloOids.flatMap((oid: String) => links.getOrElse(oid, Set(oid)))
  }
}

class OppijaNumeroRekisteri(client: VirkailijaRestClient, val system: ActorSystem) extends IOppijaNumeroRekisteri {
  private val logger = Logging.getLogger(system, this)

  override def fetchLinkedHenkiloOidsMap(henkiloOids: Set[String]): Future[Map[String, Set[String]]] = {
    val queryObject: Map[String, Set[String]] = Map("henkiloOids" -> henkiloOids)
    logger.debug(s"Querying with $queryObject")

    client.postObject[Map[String, Set[String]], Seq[HenkiloViite]]("oppijanumerorekisteri-service.duplicatesByPersonOids")(resource = queryObject, acceptedResponseCode = HttpStatus.SC_OK).map(viitteet => {
      val masterOids = viitteet.map(_.masterOid)
      val linkedOids = viitteet.map(_.henkiloOid)
      val viitteetByMasterOid = viitteet.groupBy(_.masterOid).map(kv => (kv._1, kv._2.map(_.henkiloOid)))
      val viitteetByLinkedOid = viitteet.groupBy(_.henkiloOid).map(kv => (kv._1, kv._2.map(_.masterOid)))
      val allPairs: Map[String, Seq[String]] = viitteetByMasterOid ++ viitteetByLinkedOid
      henkiloOids.map((queriedOid: String) => {
        val allAliases: Seq[String] = allPairs.getOrElse(queriedOid, Nil)
        (queriedOid, (List(queriedOid) ++ allAliases).toSet)
      }).toMap
    })
  }
}

object MockOppijaNumeroRekisteri extends IOppijaNumeroRekisteri {
  val linkedTestPersonOids = Seq("1.2.246.562.24.58099330694", "1.2.246.562.24.67587718272")

  def fetchLinkedHenkiloOidsMap(henkiloOids: Set[String]): Future[Map[String, Set[String]]] = {
    Future.successful(henkiloOids.map { queriedOid =>
      if (linkedTestPersonOids.contains(queriedOid)) {
        (queriedOid, linkedTestPersonOids.toSet)
      } else {
        (queriedOid, Set(queriedOid))
      }
    }.toMap)
  }
}

case class HenkiloViite(henkiloOid: String, masterOid: String)

case class PersonOidsWithAliases(henkiloOids: Set[String], aliasesByPersonOids: Map[String, Set[String]], henkiloOidsWithLinkedOids: Set[String]) {
  def grouped(size: Int): Iterator[PersonOidsWithAliases] = {
    henkiloOids.grouped(size).map(oidsOfGroup => {
      val aliasMapOfGroup: Map[String, Set[String]] = aliasesByPersonOids.filter(oidWithAliases => oidsOfGroup.contains(oidWithAliases._1))
      PersonOidsWithAliases(oidsOfGroup, aliasMapOfGroup)
    })
  }

  def diff(henkiloOidsToRemove: Set[String]): PersonOidsWithAliases = {
    PersonOidsWithAliases(henkiloOids -- henkiloOidsToRemove, aliasesByPersonOids -- henkiloOidsToRemove)
  }

  def isEmpty: Boolean = aliasesByPersonOids.isEmpty
}

object PersonOidsWithAliases {
  /**
    * @return a dummy object with just the given oids as their aliases, so we don't have to migrate everything at once
    */
  @Deprecated // The places where this is used should be updated to use real linked data"
  def apply(henkiloOids: Set[String]): PersonOidsWithAliases = PersonOidsWithAliases(henkiloOids, henkiloOids.map(h => (h, Set(h))).toMap, henkiloOids)

  def apply(queriedOids: Set[String], aliasesByPersonOids: Map[String, Set[String]]): PersonOidsWithAliases = {
    val combinedOidSet = IOppijaNumeroRekisteri.combineLinkedHenkiloOids(queriedOids, aliasesByPersonOids)
    PersonOidsWithAliases(queriedOids, aliasesByPersonOids, combinedOidSet)
  }
}
