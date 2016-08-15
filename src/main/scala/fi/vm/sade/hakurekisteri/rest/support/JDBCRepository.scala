package fi.vm.sade.hakurekisteri.rest.support

import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriDriver.api._
import fi.vm.sade.hakurekisteri.storage.repository.{Deleted, _}
import fi.vm.sade.hakurekisteri.storage.{Identified, ResourceService}
import slick.ast.BaseTypedType
import slick.lifted

import scala.concurrent
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}


trait JDBCRepository[R <: Resource[I, R], I, T <: JournalTable[R, I, _]] extends Repository[R,I]  {

  val journal: JDBCJournal[R,I,T]

  implicit val idType: BaseTypedType[I] = journal.idType

  override def delete(id: I, source: String): Unit = journal.addModification(Deleted[R,I](id, source))

  override def cursor(t: R): Any = ???

  val all = journal.latestResources.filter(_.deleted === false)

  def latest(id: I): lifted.Query[T, Delta[R, I], Seq] = all.filter((item) => item.resourceId === id)

  override def get(id: I): Option[R with Identified[I]] = Await.result(journal.db.run(latest(id).result.headOption), 10.seconds).collect {
    case Updated(res) => res
  }

  override def listAll(): Seq[R with Identified[I]] = Await.result(journal.db.run(all.result), 1.minute).collect {
    case Updated(res) => res
  }

  override def count: Int = Await.result(journal.db.run(all.length.result), 1.minute)

  def doSave(t: R with Identified[I]): R with Identified[I] = {
    journal.addModification(Updated[R, I](t))
    t
  }

  def deduplicationQuery(i: R)(t: T): lifted.Rep[Boolean]

  def deduplicate(i: R): Option[R with Identified[I]] = Await.result(journal.db.run(all.filter(deduplicationQuery(i)).result), 30.seconds).collect {
    case Updated(res) => res
  }.headOption

  override def save(t: R): R with Identified[I] = {
    deduplicate(t) match {
      case Some(i) => doSave(t.identify(i.id))
      case None => doSave(t.identify)
    }
  }
  override def insert(t: R): R with Identified[I] = {
    deduplicate(t) match {
      case Some(i) => i
      case None => doSave(t.identify)
    }
  }
}

trait JDBCService[R <: Resource[I, R], I, T <: JournalTable[R, I, _]] extends ResourceService[R,I] { this: JDBCRepository[R,I,T] =>
  val dbExecutor:ExecutionContext

  override def findBy(q: Query[R]): Future[Seq[R with Identified[I]]] = {
    dbQuery.lift(q).map{
      case Right(query) => journal.db.run(query.result).map(_.collect { case Updated(res) => res })(dbExecutor)
      case Left(t) => Future.failed(t)
    }.getOrElse(Future.successful(Seq()))
  }

  val dbQuery: PartialFunction[Query[R], Either[Throwable, lifted.Query[T, Delta[R, I], Seq]]]
}
