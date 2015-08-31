package fi.vm.sade.hakurekisteri.integration.haku

import akka.actor.Status.Failure
import akka.actor.{Cancellable, Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import fi.vm.sade.hakurekisteri.Config
import fi.vm.sade.hakurekisteri.dates.{Ajanjakso, InFuture}
import fi.vm.sade.hakurekisteri.integration.hakemus.{RefreshHakemukset, AktiivisetHaut}
import fi.vm.sade.hakurekisteri.integration.parametrit.{HakuParams, KierrosRequest}
import fi.vm.sade.hakurekisteri.integration.tarjonta._
import fi.vm.sade.hakurekisteri.integration.valintatulos.{BatchUpdateValintatulos, UpdateValintatulos}
import fi.vm.sade.hakurekisteri.integration.ytl.HakuList
import fi.vm.sade.hakurekisteri.tools.RicherString._
import org.joda.time.{DateTime, ReadableInstant}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions


class HakuActor(tarjonta: ActorRef, parametrit: ActorRef, hakemukset: ActorRef, valintaTulos: ActorRef, ytl: ActorRef, config: Config) extends Actor with ActorLogging {
  implicit val ec = context.dispatcher

  var activeHakus: Seq[Haku] = Seq()
  val hakuRefreshTime = config.integrations.hakuRefreshTimeHours.hours
  val hakemusRefreshTime = config.integrations.hakemusRefreshTimeHours.hours
  val valintatulosRefreshTimeHours = config.integrations.valintatulosRefreshTimeHours.hours
  var starting = true

  val update = context.system.scheduler.schedule(1.second, hakuRefreshTime, self, Update)
  var retryUpdate: Option[Cancellable] = None
  var hakemusUpdate: Option[Cancellable] = None
  var vtsUpdate: Option[Cancellable] = None

  import FutureList._

  def getHaku(q: GetHaku): Haku = {
    activeHakus.find(_.oid == q.oid) match {
      case None => throw HakuNotFoundException(s"haku not found with oid ${q.oid}")
      case Some(h) => h
    }
  }

  log.info(s"starting haku actor (hakuRefreshTime: $hakuRefreshTime, valintatulosRefreshTimeHours: $valintatulosRefreshTimeHours, hakemusRefreshTime: $hakemusRefreshTime, starting: $starting)")

  override def receive: Actor.Receive = {
    case Update =>
      log.info(s"updating all hakus for $self from ${sender()}")
      tarjonta ! GetHautQuery

    case HakuRequest => sender ! activeHakus

    case q: GetHaku => sender ! getHaku(q)

    case RestHakuResult(hakus: List[RestHaku]) => enrich(hakus).waitForAll pipeTo self

    case RefreshHakemukset => hakemukset forward RefreshHakemukset

    case sq: Seq[_] =>
      val s = sq.collect{ case h: Haku => h}
      activeHakus = s.filter(_.aika.isCurrently)
      ytl ! HakuList(activeHakus.filter(_.kkHaku).map(_.oid).toSet)
      log.info(s"current hakus [${activeHakus.map(h => h.oid).mkString(", ")}]")
      if (starting) {
        starting = false
        vtsUpdate.foreach(_.cancel())
        vtsUpdate = Some(context.system.scheduler.schedule(1.second, valintatulosRefreshTimeHours, self, RefreshSijoittelu))
        hakemusUpdate.foreach(_.cancel())
        hakemusUpdate = Some(context.system.scheduler.schedule(1.second, hakemusRefreshTime, self, ReloadHakemukset))
        log.info(s"started VTS & hakemus schedulers, starting: $starting")
      }

    case RefreshSijoittelu =>
      log.info(s"refreshing sijoittelu from ${sender()}")
      refreshKeepAlives()

    case ReloadHakemukset =>
      log.info(s"loading hakemukset from ${sender()}")
      hakemukset ! AktiivisetHaut(activeHakus.toSet)

    case Failure(t: GetHautQueryFailedException) =>
      log.error(s"${t.getMessage}, retrying in a minute")
      retryUpdate.foreach(_.cancel())
      retryUpdate = Some(context.system.scheduler.scheduleOnce(1.minute, self, Update))

    case Failure(t) =>
      log.error(t, s"got failure from ${sender()}")

  }

  def enrich(hakus: List[RestHaku]): List[Future[Haku]] = {
    for (
      haku <- hakus
      if haku.oid.isDefined && haku.hakuaikas.nonEmpty
    ) yield getKierrosEnd(haku.oid.get).map(Haku(haku))
  }

  def getKierrosEnd(hakuOid: String): Future[ReadableInstant] = {
    import akka.pattern.ask
    import akka.util.Timeout

    import scala.concurrent.duration._
    implicit val to: Timeout = 2.minutes
    (parametrit ? KierrosRequest(hakuOid)).mapTo[HakuParams].map(_.end).recover {
      case _ => InFuture
    }
  }

  def refreshKeepAlives() {
    valintaTulos.!(BatchUpdateValintatulos(activeHakus.map(h => UpdateValintatulos(h.oid)).toSet))
  }

  override def postStop(): Unit = {
    update.cancel()
    Seq(retryUpdate, hakemusUpdate, vtsUpdate).foreach(_.foreach(_.cancel()))
    super.postStop()
  }
}

object Update

object HakuRequest

object ReloadHakemukset

object RefreshSijoittelu

case class HakuNotFoundException(m: String) extends Exception(m)

case class GetHaku(oid: String)

case class Kieliversiot(fi: Option[String], sv: Option[String], en: Option[String])

case class Haku(nimi: Kieliversiot, oid: String, aika: Ajanjakso, kausi: String, vuosi: Int, koulutuksenAlkamiskausi: Option[String], koulutuksenAlkamisvuosi: Option[Int], kkHaku: Boolean)

object Haku {
  def apply(haku: RestHaku)(loppu: ReadableInstant): Haku = {
    val ajanjakso = Ajanjakso(findStart(haku), loppu)
    Haku(
      Kieliversiot(haku.nimi.get("kieli_fi").flatMap(Option(_)).flatMap(_.blankOption), haku.nimi.get("kieli_sv").flatMap(Option(_)).flatMap(_.blankOption), haku.nimi.get("kieli_en").flatMap(Option(_)).flatMap(_.blankOption)), haku.oid.get,
      ajanjakso,
      haku.hakukausiUri,
      haku.hakukausiVuosi,
      haku.koulutuksenAlkamiskausiUri,
      haku.koulutuksenAlkamisVuosi,
      kkHaku = haku.kohdejoukkoUri.exists(_.startsWith("haunkohdejoukko_12"))
    )
  }

  def findStart(haku: RestHaku): DateTime = {
    new DateTime(haku.hakuaikas.map(_.alkuPvm).sorted.head)
  }
}

class FutureList[A](futures: Seq[Future[A]]) {
  def waitForAll(implicit ec: ExecutionContext): Future[Seq[A]] = Future.sequence(futures)
}

object FutureList {
  implicit def futures2FutureList[A](futures: Seq[Future[A]]): FutureList[A] = new FutureList(futures)
}
