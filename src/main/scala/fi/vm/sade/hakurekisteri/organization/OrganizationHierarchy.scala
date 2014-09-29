package fi.vm.sade.hakurekisteri.organization

import scala.xml.Elem
import org.scalatra.util.RicherString._
import org.joda.time.DateTime
import dispatch._
import Defaults._
import akka.actor.{Cancellable, ActorRef, Actor}
import fi.vm.sade.hakurekisteri.rest.support.{Resource, Query}
import java.util.UUID
import fi.vm.sade.hakurekisteri.storage.Identified
import scala.concurrent.duration._
import akka.event.Logging
import com.ning.http.client.Response
import fi.vm.sade.hakurekisteri.storage.DeleteResource

class OrganizationHierarchy[A <: Resource[I] :Manifest, I](serviceUrl:String, filteredActor:ActorRef, organizationFinder: Function1[A,Seq[String]]) extends FutureOrganizationHierarchy[A, I](serviceUrl, filteredActor, (item: A) => Future.successful(organizationFinder(item)) )

class FutureOrganizationHierarchy[A <: Resource[I] :Manifest, I ](serviceUrl:String, filteredActor:ActorRef, organizationFinder: Function1[A, concurrent.Future[Seq[String]]]) extends OrganizationHierarchyAuthorization[A](serviceUrl, organizationFinder) with Actor {


  val logger = Logging(context.system, this)

  private var scheduledTask: Cancellable = null

  class Update

  object update extends Update

  override def preStart() {
    scheduledTask = context.system.scheduler.schedule(
      0.seconds, 60.minutes,
      self, update)
  }

  override def postStop() {
    scheduledTask.cancel()
  }

  implicit val timeout: akka.util.Timeout = 30.seconds

  def futfilt(s: Seq[A], authorizer: A => concurrent.Future[Boolean]) = {
    Future.traverse(s)((item) => authorizer(item).map((_ , item))).map(_.filter(_._1).map(_._2))
  }

  val log = Logging(context.system, this)

  import akka.pattern.ask
  import akka.pattern.pipe
  override def receive: Receive = {
    case a:Update => fetch()
    case a:OrganizationAuthorizer => logger.info("org paths loaded");authorizer = a
    case AuthorizedQuery(q,orgs,user) => (filteredActor ? q).mapTo[Seq[A with Identified[UUID]]].flatMap(futfilt(_, isAuthorized(user, orgs))) pipeTo sender
    case AuthorizedRead(id, orgs,user) => (filteredActor ? id).mapTo[Option[A with Identified[UUID]]].flatMap(checkRights(user, orgs)) pipeTo sender
    case AuthorizedDelete(id, orgs, user)  => val checkedRights = for (resourceToDelete <- filteredActor ? id;
                                                                    rights <- checkRights(user, orgs)(resourceToDelete.asInstanceOf[Option[A]]);
                                                                    result <- if (rights.isDefined) filteredActor ? DeleteResource(id, user) else Future.successful(Unit)
                                                                    )
                                                                    yield result.asInstanceOf[Unit]

                                               checkedRights pipeTo sender
    case AuthorizedCreate(resource:A, orgs , user) => filteredActor forward resource
    case AuthorizedUpdate(resource:A with Identified[I], orgs , user) => val checked = for (resourceToUpdate <- filteredActor ? resource.id;
                                                                     rightsForOld <- checkRights(user,orgs)(resourceToUpdate.asInstanceOf[Option[A]]);
                                                                     rightsForNew <- checkRights(user,orgs)(Some(resource));
                                                                     result <- if (rightsForOld.isDefined && rightsForNew.isDefined) filteredActor ? resource else Future.successful(rightsForOld)
                                                                      )
                                                                      yield result
                                                                    checked pipeTo sender
    case message:AnyRef => filteredActor forward message
  }


  def checkRights(user: String,orgs: Seq[String]) = (item:Option[A]) => item match {

    case None => Future.successful(None)
    case Some(resource: A) => isAuthorized(user, orgs)(resource).map((authorized) => if (authorized) Some(resource) else None)
  }


  def fetch() {
    val authorizer: Future[OrganizationAuthorizer] = createAuthorizer
    logger.info("fetching organizations from: " + serviceUrl)
    authorizer pipeTo self
    authorizer.onFailure {
      case e: Exception => logger.error("failed loading organizations", e)
    }

  }

}

class OrganizationHierarchyAuthorization[A:Manifest](serviceUrl:String, organizationFinder: A => Future[Seq[String]]) {



  val svc = url(serviceUrl).POST
  var authorizer = OrganizationAuthorizer(Map())


  def addSelfToPaths(m:Map[String,Seq[String]], org:Org) = {
    m + (org.oid -> Seq(org.oid))
  }

  def addParentToPaths(m:Map[String,Seq[String]], org:Org) = {
    val addedParents = org.parent match {
      case None => Map[String,Seq[String]]()
      case Some(parent) => m.filter((t) => t._2.head.equals(org.oid)).map((t) => t._1 -> (parent +: t._2))
    }
    m ++ addedParents
  }






  def createAuthorizer: Future[OrganizationAuthorizer] =  edgeFetch map OrganizationAuthorizer

  def readXml: Future[Elem] = {
    val result: Future[Response] = Http(svc << <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:typ="http://model.api.organisaatio.sade.vm.fi/types">
      <soapenv:Header/> <soapenv:Body>
        <typ:getOrganizationStructure></typ:getOrganizationStructure>
      </soapenv:Body>
    </soapenv:Envelope>.toString)

    val soapFuture: concurrent.Future[Elem] = result map ((response) => scala.xml.XML.load(new java.io.InputStreamReader(response.getResponseBodyAsStream, "UTF-8")))
    soapFuture
  }

  def possibleEdges(soapFuture: concurrent.Future[Elem]):concurrent.Future[Seq[(Option[String], Option[String])]] = {
    val orgTagsFuture = soapFuture map (_ \ "Body" \ "getOrganizationStructureResponse" \ "organizationStructure")
    orgTagsFuture map (_.map((org) => ((org \ "@parentOid").text.blankOption, (org \ "@oid").text.blankOption)))
  }


  def findEdges(soapFuture: concurrent.Future[Elem]): concurrent.Future[Seq[(String,String)]] = {
    val rawList = possibleEdges(soapFuture)
    rawList.map (_.collect { case (Some(parent:String), Some(child:String)) => (parent,child)} )

  }

  def childOrgs(edges: Seq[(String, String)]): Set[String] = {
    edges.map(_._2).toSet
  }

  def parentOrgs(edges: Seq[(String, String)]): Set[String] = {
    edges.map(_._1).toSet
  }

  def leafOrgs(edges: Seq[(String,String)]): Set[String]= {
    childOrgs(edges) -- parentOrgs(edges)
  }

  def splitWith[T](s:Seq[T], p:T => Boolean ): (Seq[T], Seq[T]) = {
    ((Seq[T](), Seq[T]()) /: s) ((a, item) => if (p(item)) (item +: a._1, a._2) else (a._1, item +: a._2))
  }

  def findNonLeavesAndLeavesLeafEdges(edges: Seq[(String,String)]): (Seq[(String, String)], Seq[(String, String)]) = {
    val leaves = leafOrgs(edges)
    splitWith(edges:Seq[(String,String)], (edge:(String,String)) => leaves.contains(edge._2))
  }

  def findPaths(parentEdges: Seq[(String,String)], leafEdges: Seq[(String,String)], accumulator:Map[String, Seq[String]]): Map[String, Seq[String]] = {
    val leafMap: Map[String, String] = leafEdges.map((edge) => edge._2 -> edge._1).toMap
    val needAddition: Map[String, Seq[String]] = accumulator.map((kv) => {
      val addedPathKeys = kv._2.collect(leafMap)
      val newPath = addedPathKeys ++ kv._2
      kv._1 -> newPath
    }) ++ leafMap.map((kv: (String, String)) => kv._1 -> Seq(kv._2, kv._1))
    val  (newLeaves, newOthers)  = findNonLeavesAndLeavesLeafEdges(parentEdges)
    if (parentEdges.nonEmpty) findPaths(newOthers, newLeaves, needAddition)
    else needAddition ++ leafMap.values.map((v) => v -> Seq(v))
  }

  def edgeBuild(edges:Seq[(String,String)]) = {
    findPaths(edges, Seq(), Map())
  }

  def edgeFetch: concurrent.Future[Map[String, Seq[String]]] = {
    val edgeFuture = findEdges(readXml)
    edgeFuture map edgeBuild
  }



  def isAuthorized(user:String, orgs: Seq[String])(item: A): concurrent.Future[Boolean] = authorizer.checkAccess(user, orgs, organizationFinder(item))


}

case class AuthorizedQuery[A](q: Query[A], orgs: Seq[String], user:String)
case class AuthorizedRead[I](id: I, orgs: Seq[String], user: String)

case class AuthorizedDelete[I](id: I, orgs: Seq[String], user: String)
case class AuthorizedCreate[A <: Resource[_]](q: A, orgs: Seq[String], user: String)
case class AuthorizedUpdate[A <: Resource[_]](q: A with Identified[_], orgs: Seq[String], user: String)



case class OrganizationAuthorizer(orgPaths: Map[String, Seq[String]]) {
  def checkAccess(user: String, orgs: Seq[String], futTarget: concurrent.Future[Seq[String]]) = futTarget.map {
    (target) =>
    val paths: Seq[String] = target.flatMap((oid) => orgPaths.getOrElse(oid, Seq("1.2.246.562.10.00000000001", oid)))
    paths.exists { x => user == x || orgs.contains(x) }
  }
}
case class Org(oid: String, parent: Option[String], lopetusPvm: Option[DateTime] )
