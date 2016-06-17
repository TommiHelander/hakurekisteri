package fi.vm.sade.hakurekisteri.opiskelija

import akka.event.Logging
import com.github.nscala_time.time.Imports._
import fi.vm.sade.hakurekisteri.rest.support.Kausi._
import fi.vm.sade.hakurekisteri.rest.support.{JDBCJournal, JDBCRepository, JDBCService, Query}
import fi.vm.sade.hakurekisteri.storage._
import fi.vm.sade.hakurekisteri.storage.repository._
import java.util.UUID

import fi.vm.sade.hakurekisteri.rest.support

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.lifted.{Column, Query}


trait OpiskelijaRepository extends JournaledRepository[Opiskelija, UUID] {

  var henkiloIndex: Map[String, Seq[Opiskelija with Identified[UUID]]] = Option(henkiloIndex).getOrElse(Map())
  var oppilaitosIndex: Map[(String, String), Seq[Opiskelija with Identified[UUID]]] = Option(oppilaitosIndex).getOrElse(Map())

  def addNew(opiskelija: Opiskelija with Identified[UUID]) = {
    henkiloIndex = Option(henkiloIndex).getOrElse(Map())
    henkiloIndex = henkiloIndex  + (opiskelija.henkiloOid -> (opiskelija +: henkiloIndex.getOrElse(opiskelija.henkiloOid, Seq())))
    oppilaitosIndex = Option(oppilaitosIndex).getOrElse(Map())
    val startYear = opiskelija.alkuPaiva.getYear
    val endYear = opiskelija.loppuPaiva.getOrElse(new DateTime()).getYear
    for (year <- startYear to endYear) oppilaitosIndex = oppilaitosIndex + ((opiskelija.oppilaitosOid, year.toString) -> (opiskelija +: oppilaitosIndex.getOrElse((opiskelija.oppilaitosOid, year.toString), Seq())))
  }

  override def index(old: Option[Opiskelija with Identified[UUID]], current: Option[Opiskelija with Identified[UUID]]) {
    def removeOld(opiskelija: Opiskelija with Identified[UUID]) = {
      henkiloIndex = Option(henkiloIndex).getOrElse(Map())
      henkiloIndex = henkiloIndex.get(opiskelija.henkiloOid).
        map(_.filter((a) => a != opiskelija || a.id != opiskelija.id)).
        map((ns) => henkiloIndex + (opiskelija.henkiloOid -> ns)).getOrElse(henkiloIndex)

      oppilaitosIndex = Option(oppilaitosIndex).getOrElse(Map())
      val startYear = opiskelija.alkuPaiva.getYear
      val endYear = opiskelija.loppuPaiva.getOrElse(new DateTime()).getYear
      for (year <- startYear to endYear) {
        val opiskelijat = oppilaitosIndex.getOrElse((opiskelija.oppilaitosOid, year.toString), Seq()).filterNot(_.id == opiskelija.id)
        if (opiskelijat.isEmpty)
          oppilaitosIndex = oppilaitosIndex - ((opiskelija.oppilaitosOid, year.toString))
        else
          oppilaitosIndex = oppilaitosIndex + ((opiskelija.oppilaitosOid, year.toString) -> opiskelijat)
      }

    }

    old.foreach(removeOld)
    current.foreach(addNew)
  }


}

trait OpiskelijaService extends InMemQueryingResourceService[Opiskelija, UUID] with OpiskelijaRepository {

  override val emptyQuery: PartialFunction[support.Query[Opiskelija], Boolean] = {
    case OpiskelijaQuery(None, None, None, None, None, None) => true
  }

  override val matcher: PartialFunction[support.Query[Opiskelija], (Opiskelija with Identified[UUID]) => Boolean] = {
    case OpiskelijaQuery(henkilo, kausi, vuosi, paiva, oppilaitosOid, luokka) =>
      (o: Opiskelija with Identified[UUID]) => checkHenkilo(henkilo)(o) &&
                                         checkVuosiAndKausi(vuosi, kausi)(o) &&
                                         checkPaiva(paiva)(o) &&
                                         checkOppilaitos(oppilaitosOid)(o) &&
                                         checkLuokka(luokka)(o)
  }

  override val optimize: PartialFunction[support.Query[Opiskelija], Future[Seq[Opiskelija with Identified[UUID]]]] = {
    case OpiskelijaQuery(Some(henkilo), None, None, None, None, None) =>
      Future { henkiloIndex.getOrElse(henkilo, Seq()) }

    case OpiskelijaQuery(None, None, Some(vuosi), None, Some(oppilaitosOid), None) =>
      Future { oppilaitosIndex.getOrElse((oppilaitosOid, vuosi), Seq()) }

    case OpiskelijaQuery(None, None, None, None, Some(oppilaitosOid), None) =>
      Future {
        oppilaitosIndex.collect {
          case ((oid, _), value) if oid == oppilaitosOid => value
        }.foldLeft[Seq[Opiskelija with Identified[UUID]]](Seq())(_ ++ _)
      }

    case OpiskelijaQuery(Some(henkilo), kausi, vuosi, paiva, oppilaitosOid, luokka) =>
      Future {
        henkiloIndex.getOrElse(henkilo, Seq())
      } flatMap(filtered => executeQuery(filtered)(OpiskelijaQuery(Some(henkilo), kausi, vuosi, paiva, oppilaitosOid, luokka)))

    case OpiskelijaHenkilotQuery(henkilot) =>
      Future {
        henkiloIndex.collect {
          case (oid, value) if henkilot.contains(oid) => value
        }.foldLeft[Seq[Opiskelija with Identified[UUID]]](Seq())(_ ++ _)
      }
  }

  def checkOppilaitos(oppilaitos:Option[String])(o:Opiskelija):Boolean = oppilaitos match {
    case Some(oid) => o.oppilaitosOid.equals(oid)
    case None => true
  }

  def checkLuokka(luokka:Option[String])(o:Opiskelija) = luokka match {
    case Some(l) => o.luokka.equals(l)
    case None => true
  }

  def checkPaiva(paiva:Option[DateTime])(o:Opiskelija) = paiva match {
    case Some(date) => o.loppuPaiva match {
      case Some(end) =>  (new DateTime(o.alkuPaiva) to new DateTime(end)).contains(date)
      case None => new DateTime(o.alkuPaiva).isBefore(date)
    }
    case None => true
  }

  def checkHenkilo(henkilo: Option[String])(o:Opiskelija):Boolean  =  henkilo match {
    case Some(oid) => o.henkiloOid.equals(oid)
    case None => true
  }

  def checkVuosiAndKausi(vuosi: Option[String], kausi: Option[Kausi])(o:Opiskelija): Boolean = vuosi match {
    case Some(v) => during(getInterval(v.toInt,kausi), o.alkuPaiva, o.loppuPaiva)
    case None => duringKausi(kausi, o.alkuPaiva, o.loppuPaiva)

  }

  def during(interval: Interval, start: DateTime, end: Option[DateTime]) = end match {
    case Some(date) => new DateTime(start) to new DateTime(date) overlaps interval
    case None => interval.end > new DateTime(start)

  }

  def getInterval(vuosi: Int, kausi: Option[Kausi]): Interval = kausi match {
    case None => year(vuosi)
    case Some(Kevät) => newYear(vuosi).toDateTimeAtStartOfDay to startOfAutumn(vuosi).toDateTimeAtStartOfDay
    case Some(Syksy) => startOfAutumn(vuosi).toDateTimeAtStartOfDay to newYear(vuosi + 1).toDateTimeAtStartOfDay
    case Some(_) => throw new IllegalArgumentException("Not a kausi")
  }

  def year(y:Int): Interval = {
    new Interval(newYear(y).toDateTimeAtStartOfDay, 1.year)
  }

  def newYear(y: Int): LocalDate = {
    new MonthDay(1, 1).toLocalDate(y)
  }

  def duringKausi(kausi: Option[Kausi], start: DateTime, end: Option[DateTime]): Boolean = (kausi, end) match {
    case (Some(Syksy), Some(date)) => (new DateTime(date).getYear != new DateTime(start).getYear) || startOfAutumn(new DateTime(date).getYear).toDateTimeAtStartOfDay <= new DateTime(date)
    case (Some(Kevät), Some(date)) => (new DateTime(date).getYear != new DateTime(start).getYear) || startOfAutumn(new DateTime(date).getYear).toDateTimeAtStartOfDay > new DateTime(start)
    case (Some(_), _ ) => throw new IllegalArgumentException("Not a kausi")
    case (None, _) => true
    case (_, None) => true

  }

  def startOfAutumn(v:Int): LocalDate = {
    new MonthDay(8, 1).toLocalDate(v)
  }

}

class OpiskelijaActor(val journal:Journal[Opiskelija, UUID] = new InMemJournal[Opiskelija, UUID]) extends ResourceActor[Opiskelija, UUID] with OpiskelijaService {
  override val logger = Logging(context.system, this)
}

import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriDriver.simple._
import scala.slick.lifted

class OpiskelijaJDBCActor(val journal: JDBCJournal[Opiskelija, UUID, OpiskelijaTable], poolSize: Int)
  extends ResourceActor[Opiskelija, UUID] with JDBCRepository[Opiskelija, UUID, OpiskelijaTable] with JDBCService[Opiskelija, UUID, OpiskelijaTable] {

  override def deduplicationQuery(i: Opiskelija)(t: OpiskelijaTable): lifted.Column[Boolean] =
    t.oppilaitosOid === i.oppilaitosOid && t.luokkataso === i.luokkataso && t.henkiloOid === i.henkiloOid

  override val dbExecutor: ExecutionContext = context.dispatcher
  override val dbQuery: PartialFunction[support.Query[Opiskelija], lifted.Query[OpiskelijaTable, Delta[Opiskelija, UUID], Seq]] = {
    case OpiskelijaQuery(henkilo, kausi, vuosi, paiva, oppilaitosOid, luokka) =>
      all.filter(t => matchHenkilo(henkilo)(t) && matchOppilaitosOid(oppilaitosOid)(t) && matchLuokka(luokka)(t))
  }

  private def matchHenkilo(henkilo: Option[String])(t: OpiskelijaTable): Column[Boolean] = henkilo match {
    case Some(h) => t.henkiloOid === h
    case None => true
  }

  private def matchOppilaitosOid(oppilaitosOid: Option[String])(t: OpiskelijaTable): Column[Boolean] = oppilaitosOid match {
    case Some(o) => t.oppilaitosOid === o
    case None => true
  }

  private def matchLuokka(luokka: Option[String])(t: OpiskelijaTable): Column[Boolean] = luokka match {
    case Some(l) => t.luokka === l
    case None => true
  }

  // TODO implement the remaining matchers

}
