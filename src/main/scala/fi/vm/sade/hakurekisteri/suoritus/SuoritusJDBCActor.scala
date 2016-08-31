package fi.vm.sade.hakurekisteri.suoritus

import java.util.UUID
import java.util.concurrent.Executors

import akka.dispatch.ExecutionContexts
import com.github.nscala_time.time.Imports._
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriDriver.api._
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriDriver.{startOfAutumnDate, yearOf}
import fi.vm.sade.hakurekisteri.rest.support.Kausi._
import fi.vm.sade.hakurekisteri.rest.support.{Query, Kausi => _, _}
import fi.vm.sade.hakurekisteri.storage.ResourceActor
import fi.vm.sade.hakurekisteri.storage.repository.Delta
import slick.lifted
import slick.lifted.Rep

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class SuoritusJDBCActor(val journal: JDBCJournal[Suoritus, UUID, SuoritusTable], poolSize: Int)
  extends ResourceActor[Suoritus, UUID] with JDBCRepository[Suoritus, UUID, SuoritusTable] with JDBCService[Suoritus, UUID, SuoritusTable] {

  override def deduplicationQuery(o: Suoritus)(t: SuoritusTable): Rep[Boolean] = o match {
    case VapaamuotoinenSuoritus(henkilo, _, _, _, tyyppi, index, _) =>
      t.henkiloOid === henkilo && (t.tyyppi === tyyppi).asColumnOf[Boolean] && (t.index === index).asColumnOf[Boolean]
    case VirallinenSuoritus(komo, myontaja, _, _, henkilo, _, _, _, vahv, _) =>
      (t.komo === komo).asColumnOf[Boolean] && t.myontaja === myontaja && t.henkiloOid === henkilo && (t.vahvistettu === vahv).asColumnOf[Boolean]
  }

  override val dbExecutor: ExecutionContext = ExecutionContexts.fromExecutor(Executors.newFixedThreadPool(poolSize))

  override val dbQuery: PartialFunction[Query[Suoritus], Either[Throwable, lifted.Query[SuoritusTable, Delta[Suoritus, UUID], Seq]]] = {
    case SuoritusQuery(henkilo, kausi, vuosi, myontaja, komo, muokattuJalkeen) =>
      Right(latest(journal.table.filter(t => matchHenkilo(henkilo)(t) && matchKausi(kausi)(t) && matchVuosi(vuosi)(t) &&
        matchMyontaja(myontaja)(t) && matchKomo(komo)(t) && matchMuokattuJalkeen(muokattuJalkeen)(t))))
    case SuoritusHenkilotQuery(henkilot) => Right(latest(journal.table.filter(_.henkiloOid.inSet(henkilot))))
    case SuoritysTyyppiQuery(henkilo, komo) => Right(latest(journal.table.filter(t => matchHenkilo(Some(henkilo))(t) && matchKomo(Some(komo))(t))))
    case AllForMatchinHenkiloSuoritusQuery(vuosi, myontaja) => Right(latest(journal.table.filter(t => matchVuosi(vuosi)(t) && matchMyontaja(myontaja)(t))))
  }

  private def matchHenkilo(henkilo: Option[String])(s: SuoritusTable): Rep[Boolean] =
    henkilo.fold[Rep[Boolean]](true)(h => s.henkiloOid === h)

  private def matchKausi(kausi: Option[Kausi])(s: SuoritusTable): Rep[Boolean] =
    kausi.fold[Rep[Boolean]](true) {
      case Kevät =>
        s.valmistuminen.isDefined && (s.valmistuminen < startOfAutumnDate(yearOf(s.valmistuminen))).asColumnOf[Boolean]
      case Syksy =>
        s.valmistuminen.isDefined && (startOfAutumnDate(yearOf(s.valmistuminen)) <= s.valmistuminen).asColumnOf[Boolean]
    }

  private def matchMuokattuJalkeen(muokattuJalkeen: Option[DateTime])(s: SuoritusTable): Rep[Boolean] = {
    muokattuJalkeen.fold[Rep[Boolean]](true)(d => s.inserted.asColumnOf[DateTime] >= d)
  }

  private def matchKomo(komo: Option[String])(s: SuoritusTable): Rep[Boolean] =
    komo.fold[Rep[Boolean]](true)(k => s.komo.isDefined && (s.komo === k).asColumnOf[Boolean])

  private def matchVuosi(vuosi: Option[String])(s: SuoritusTable): Rep[Boolean] =
    vuosi.fold[Rep[Boolean]](true)(v => (s.vuosi.isDefined && (s.vuosi === v.toInt).asColumnOf[Boolean]) ||
      (s.valmistuminen.isDefined && (yearOf(s.valmistuminen) === vuosi).asColumnOf[Boolean]))

  private def matchMyontaja(myontaja: Option[String])(s: SuoritusTable): Rep[Boolean] =
    myontaja.fold[Rep[Boolean]](true)(m => s.myontaja === m)
}