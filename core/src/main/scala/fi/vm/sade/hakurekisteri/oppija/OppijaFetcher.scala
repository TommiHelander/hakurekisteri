package fi.vm.sade.hakurekisteri.oppija

import fi.vm.sade.hakurekisteri.rest.support.{Query, User, Registers}
import akka.actor.ActorRef
import org.joda.time.{DateTime, LocalDate}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import fi.vm.sade.hakurekisteri.integration.hakemus.{FullHakemus, HakemusQuery}
import fi.vm.sade.hakurekisteri.suoritus.{SuoritusHenkilotQuery, SuoritusQuery, Suoritus}
import fi.vm.sade.hakurekisteri.storage.Identified
import java.util.UUID
import fi.vm.sade.hakurekisteri.organization.AuthorizedQuery
import fi.vm.sade.hakurekisteri.arvosana.{Arvosana, ArvosanaQuery}
import fi.vm.sade.hakurekisteri.ensikertalainen.{EnsikertalainenQuery, Ensikertalainen}
import fi.vm.sade.hakurekisteri.opiskeluoikeus.{OpiskeluoikeusHenkilotQuery, OpiskeluoikeusQuery, Opiskeluoikeus}
import fi.vm.sade.hakurekisteri.opiskelija.{OpiskelijaHenkilotQuery, OpiskelijaQuery, Opiskelija}
import akka.pattern.ask

trait OppijaFetcher {

  val rekisterit: Registers
  val hakemusRekisteri: ActorRef
  val ensikertalaisuus: ActorRef

  val megaQueryTreshold = 10000
  val singleSplitQuerySize = 5000

  protected implicit def executor: ExecutionContext
  implicit val defaultTimeout: Timeout

  def fetchOppijat(q: HakemusQuery, ensikertalaisuudenRajapvm: Option[DateTime] = None)(implicit user: User): Future[Seq[Oppija]] =
    for (
      hakemukset <- (hakemusRekisteri ? q).mapTo[Seq[FullHakemus]];
      oppijat <- fetchOppijatFor(hakemukset, ensikertalaisuudenRajapvm)
    ) yield oppijat

  private def isMegaQuery(persons: Set[(String, Option[String])]): Boolean = persons.size > megaQueryTreshold

  def fetchOppijatFor(hakemukset: Seq[FullHakemus], ensikertalaisuudenRajapvm: Option[DateTime] = None)(implicit user: User): Future[Seq[Oppija]] = {
    val persons = extractPersons(hakemukset)

    if (isMegaQuery(persons)) {
      enrichWithEnsikertalaisuus(persons, getRekisteriData(persons.map(_._1)), ensikertalaisuudenRajapvm)
    } else {
      Future.sequence(persons.map {
        case (personOid, hetu) => fetchOppijaData(personOid, hetu, ensikertalaisuudenRajapvm)
      }.toSeq)
    }
  }

  private def extractPersons(hakemukset: Seq[FullHakemus]): Set[(String, Option[String])] =
    (for (
      hakemus <- hakemukset
      if hakemus.personOid.isDefined && hakemus.stateValid
    ) yield (hakemus.personOid.get, hakemus.hetu)).toSet

  private def enrichWithEnsikertalaisuus(persons: Set[(String, Option[String])],
                                         rekisteriData: Future[Seq[Oppija]],
                                         ensikertalaisuudenRajapvm: Option[DateTime])(implicit user: User): Future[Seq[Oppija]] = {
    val hetuMap = persons.groupBy(_._1).mapValues(_.headOption.flatMap(_._2))

    rekisteriData.flatMap(o => Future.sequence(o.map(oppija => for (
      ensikertalaisuus <- fetchEnsikertalaisuus(
        oppija.oppijanumero,
        hetuMap.getOrElse(oppija.oppijanumero, None),
        oppija.suoritukset.map(_.suoritus),
        oppija.opiskeluoikeudet,
        ensikertalaisuudenRajapvm
      )
    ) yield oppija.copy(
        ensikertalainen = ensikertalaisuus.map(_.ensikertalainen)
      )
    )))
  }

  private def getRekisteriData(personOids: Set[String])(implicit user: User): Future[Seq[Oppija]] = {
    val oppijaData = for (
      suoritukset <- fetchSuoritukset(personOids);
      todistukset <- fetchTodistukset(suoritukset);
      opiskeluoikeudet <- fetchOpiskeluoikeudet(personOids);
      opiskelijat <- fetchOpiskelu(personOids)
    ) yield (opiskelijat.groupBy(_.henkiloOid), opiskeluoikeudet.groupBy(_.henkiloOid), todistukset.groupBy(_.suoritus.henkiloOid))

    oppijaData.map {
      case (opiskelijat, opiskeluoikeudet, todistukset) =>
        (opiskelijat.keySet ++ opiskeluoikeudet.keySet ++ todistukset.keySet).map((oid: String) => {
          Oppija(
            oppijanumero = oid,
            opiskelu = opiskelijat.getOrElse(oid, Seq()),
            opiskeluoikeudet = opiskeluoikeudet.getOrElse(oid, Seq()),
            suoritukset = todistukset.getOrElse(oid, Seq()),
            ensikertalainen = None
          )
        }).toSeq
    }
  }

  private def fetchTodistukset(suoritukset: Seq[Suoritus with Identified[UUID]])(implicit user: User): Future[Seq[Todistus]] = Future.sequence(
    for (
      suoritus <- suoritukset
    ) yield for (
      arvosanat <- (rekisterit.arvosanaRekisteri ? AuthorizedQuery(ArvosanaQuery(suoritus = Some(suoritus.id)), user)).mapTo[Seq[Arvosana]]
    ) yield Todistus(suoritus, arvosanat)
  )

  private def fetchOppijaData(henkiloOid: String, hetu: Option[String], ensikertalaisuudenRajapvm: Option[DateTime])(implicit user: User): Future[Oppija] =
    for (
      suoritukset <- fetchSuoritukset(henkiloOid);
      todistukset <- fetchTodistukset(suoritukset);
      opiskelu <- fetchOpiskelu(henkiloOid);
      opiskeluoikeudet <- fetchOpiskeluoikeudet(henkiloOid);
      ensikertalainen <- fetchEnsikertalaisuus(henkiloOid, hetu, suoritukset, opiskeluoikeudet, ensikertalaisuudenRajapvm)
    ) yield Oppija(
      oppijanumero = henkiloOid,
      opiskelu = opiskelu,
      suoritukset = todistukset,
      opiskeluoikeudet = opiskeluoikeudet,
      ensikertalainen = ensikertalainen.map(_.ensikertalainen)
    )

  private def fetchEnsikertalaisuus(henkiloOid: String,
                                    hetu: Option[String],
                                    suoritukset: Seq[Suoritus],
                                    opiskeluoikeudet: Seq[Opiskeluoikeus],
                                    ensikertalaisuudenRajapvm: Option[DateTime]): Future[Option[Ensikertalainen]] = {
    val ensikertalainen: Future[Ensikertalainen] = (ensikertalaisuus ? EnsikertalainenQuery(henkiloOid, Some(suoritukset), Some(opiskeluoikeudet), ensikertalaisuudenRajapvm)).mapTo[Ensikertalainen]
    hetu match {
      case Some(_) => ensikertalainen.map(Some(_))
      case None => ensikertalainen.map(e => if (e.ensikertalainen) None else Some(e))
    }
  }

  private def fetchOpiskeluoikeudet(henkiloOid: String)(implicit user: User): Future[Seq[Opiskeluoikeus]] =
    (rekisterit.opiskeluoikeusRekisteri ? AuthorizedQuery(OpiskeluoikeusQuery(henkilo = Some(henkiloOid)), user)).mapTo[Seq[Opiskeluoikeus]]

  private def fetchOpiskeluoikeudet(henkilot: Set[String])(implicit user: User): Future[Seq[Opiskeluoikeus]] =
    splittedQuery[Opiskeluoikeus, Opiskeluoikeus](henkilot, rekisterit.opiskeluoikeusRekisteri, (henkilot) => OpiskeluoikeusHenkilotQuery(henkilot))

  private def fetchOpiskelu(henkiloOid: String)(implicit user: User): Future[Seq[Opiskelija]] =
    (rekisterit.opiskelijaRekisteri ? AuthorizedQuery(OpiskelijaQuery(henkilo = Some(henkiloOid)), user)).mapTo[Seq[Opiskelija]]

  private def fetchOpiskelu(henkilot: Set[String])(implicit user: User): Future[Seq[Opiskelija]] =
    splittedQuery[Opiskelija, Opiskelija](henkilot, rekisterit.opiskelijaRekisteri, (henkilot) => OpiskelijaHenkilotQuery(henkilot))

  private def fetchSuoritukset(henkiloOid: String)(implicit user: User): Future[Seq[Suoritus with Identified[UUID]]] =
    (rekisterit.suoritusRekisteri ? AuthorizedQuery(SuoritusQuery(henkilo = Some(henkiloOid)), user)).mapTo[Seq[Suoritus with Identified[UUID]]]

  private def fetchSuoritukset(henkilot: Set[String])(implicit user: User): Future[Seq[Suoritus with Identified[UUID]]] =
    splittedQuery[Suoritus with Identified[UUID], Suoritus](henkilot, rekisterit.suoritusRekisteri, (henkilot) => SuoritusHenkilotQuery(henkilot))

  private def splittedQuery[A, B](henkilot: Set[String], actor: ActorRef, q: (Set[String]) => Query[B])(implicit user: User): Future[Seq[A]] =
    Future.sequence(grouped(henkilot, singleSplitQuerySize).map(henkiloSubset =>
      (actor ? AuthorizedQuery(q(henkiloSubset), user)).mapTo[Seq[A]]
    )).map(_.foldLeft[Seq[A]](Seq())(_ ++ _))

  private def grouped[A](xs: Set[A], size: Int) = {
    def grouped[A](xs: Set[A], size: Int, result: Set[Set[A]]): Set[Set[A]] = {
      if(xs.isEmpty) {
        result
      } else {
        val (slice, rest) = xs.splitAt(size)
        grouped(rest, size, result + slice)
      }
    }
    grouped(xs, size, Set[Set[A]]())
  }
}
