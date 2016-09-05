package fi.vm.sade.hakurekisteri.rest.support

import akka.actor.ActorLogging
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriDriver.api._
import fi.vm.sade.hakurekisteri.storage.repository.{Deleted, _}
import fi.vm.sade.hakurekisteri.storage.{Identified, ResourceService}
import slick.ast.BaseTypedType
import slick.lifted

import scala.compat.Platform
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}


trait JDBCRepository[R <: Resource[I, R], I, T <: JournalTable[R, I, _]] extends Repository[R,I]  {

  val journal: JDBCJournal[R,I,T]

  implicit val dbExecutor: ExecutionContext
  implicit val idType: BaseTypedType[I] = journal.idType

  override def delete(id: I, source: String): Unit =
    journal.runAsSerialized(10, 5.milliseconds, s"Deleting $id", DBIO.seq(
      latest(id).map(_.current).update(false),
      journal.table += Deleted(id, source)
    )) match {
      case Right(_) => ()
      case Left(e) => throw e
    }

  override def cursor(t: R): Any = ???

  val all = journal.latestResources.filter(_.deleted === false)

  def latest(id: I): lifted.Query[T, Delta[R, I], Seq] = all.filter((item) => item.resourceId === id)

  override def get(id: I): Option[R with Identified[I]] = Await.result(journal.db.run(latest(id).result.headOption), 10.seconds).collect {
    case Updated(res) => res
  }

  override def listAll(): Seq[R with Identified[I]] = Await.result(journal.db.run(all.result), 1.minute).collect {
    case Updated(res) => res
  }

  def deduplicationQuery(i: R)(t: T): lifted.Rep[Boolean]

  private def deduplicate(i: R): DBIO[Option[R with Identified[I]]] = all.filter(deduplicationQuery(i)).result.map(_.collect {
    case Updated(res) => res
  }.headOption)

  override def save(t: R): R with Identified[I] =
    journal.runAsSerialized(10, 5.milliseconds, s"Saving $t", for {
      identified <- deduplicate(t).map(_.fold(t.identify)(duplicate => t.identify(duplicate.id)))
      _ <- all.filter(deduplicationQuery(t)).map(_.current).update(false)
      _ <- journal.table += Updated(identified)
    } yield identified) match {
      case Right(r) => r
      case Left(e) => throw e
    }

  override def insert(t: R): R with Identified[I] =
    journal.runAsSerialized(10, 5.milliseconds, s"Inserting $t",
      deduplicate(t).flatMap(_.fold({
        val identified = t.identify
        (journal.table += Updated(identified)).andThen(DBIO.successful(identified))
      })(DBIO.successful))
    ) match {
      case Right(r) => r
      case Left(e) => throw e
    }
}

trait JDBCService[R <: Resource[I, R], I, T <: JournalTable[R, I, _]] extends ResourceService[R,I] { this: JDBCRepository[R,I,T] with ActorLogging =>
  val slowQuery: Long = 100

  override def findBy(q: Query[R]): Future[Seq[R with Identified[I]]] = {
    dbQuery.lift(q).map{
      case Right(query) =>
        val start = Platform.currentTime
        val f = journal.db.run(query.result).map(_.collect { case Updated(res) => res })(dbExecutor)
        f.onComplete(_ => {
          val runtime = Platform.currentTime - start
          if (runtime > slowQuery) {
            log.info(s"Query $query took $runtime ms")
          }
        })(dbExecutor)
        f
      case Left(t) => Future.failed(t)
    }.getOrElse(Future.successful(Seq()))
  }

  val dbQuery: PartialFunction[Query[R], Either[Throwable, lifted.Query[T, Delta[R, I], Seq]]]
}
