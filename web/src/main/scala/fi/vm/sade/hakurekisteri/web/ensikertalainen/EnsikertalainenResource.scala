package fi.vm.sade.hakurekisteri.web.ensikertalainen

import java.util.concurrent.ExecutionException

import akka.actor.{ActorRef, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.ask
import fi.vm.sade.hakurekisteri.ensikertalainen.{Ensikertalainen, EnsikertalainenQuery}
import fi.vm.sade.hakurekisteri.integration.PreconditionFailedException
import fi.vm.sade.hakurekisteri.integration.hakemus.{FullHakemus, HakemusQuery}
import fi.vm.sade.hakurekisteri.rest.support.HakurekisteriJsonSupport
import fi.vm.sade.hakurekisteri.web.HakuJaValintarekisteriStack
import fi.vm.sade.hakurekisteri.web.rest.support.{IncidentReport, Security, SecuritySupport}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerEngine}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

case class ParamMissingException(message: String) extends IllegalArgumentException(message)

class EnsikertalainenResource(ensikertalainenActor: ActorRef, val hakemusRekisteri: ActorRef)
                             (implicit val sw: Swagger, system: ActorSystem, val security: Security) extends HakuJaValintarekisteriStack with HakurekisteriJsonSupport with EnsikertalainenSwaggerApi with JacksonJsonSupport with FutureSupport with SecuritySupport {

  override protected def applicationDescription: String = "Korkeakouluhakujen kiintiöiden ensikertalaisuuden kyselyrajapinta"
  override protected implicit def swagger: SwaggerEngine[_] = sw
  override protected implicit def executor: ExecutionContext = system.dispatcher
  override val logger: LoggingAdapter = Logging.getLogger(system, this)

  before() {
    contentType = formats("json")
  }

  def ensikertalaisuudenRajapvm(d: Option[String]): Option[DateTime] = d.flatMap(date => Try(ISODateTimeFormat.dateTimeParser.parseDateTime(date)).toOption)

  get("/", operation(query)) {
    try {
      val henkiloOid = params("henkilo")
      val rajapvm = ensikertalaisuudenRajapvm(params.get("ensikertalaisuudenRajapvm"))

      new AsyncResult() {
        override implicit def timeout: Duration = 60.seconds
        override val is =
          (ensikertalainenActor ? EnsikertalainenQuery(Set(henkiloOid), paivamaara = rajapvm))(60.seconds).mapTo[Seq[Ensikertalainen]].map(_.head)
      }
    } catch {
      case t: NoSuchElementException => throw ParamMissingException("parameter henkilo missing")
    }
  }

  get("/:hakuOid", operation(hakuQuery)) {
    try {
      val hakuOid = params("hakuOid")
      val rajapvm = ensikertalaisuudenRajapvm(params.get("ensikertalaisuudenRajapvm"))

      new AsyncResult() {
        override implicit def timeout: Duration = 120.seconds
        override val is = {
          val henkiloOids = (hakemusRekisteri ? HakemusQuery(Some(hakuOid), None, None, None))(60.seconds)
            .mapTo[Seq[FullHakemus]]
            .map(_.flatMap(_.personOid).toSet)
          henkiloOids.flatMap(persons => (ensikertalainenActor ? EnsikertalainenQuery(persons, paivamaara = rajapvm))(120.seconds).mapTo[Seq[Ensikertalainen]])
        }
      }
    } catch {
      case t: NoSuchElementException => throw ParamMissingException("parameter haku missing")
    }
  }

  post("/", operation(postQuery)) {
    val personOids = parse(request.body).extract[Set[String]]
    if (personOids.isEmpty) throw ParamMissingException("request body does not contain person oids")
    val rajapvm = ensikertalaisuudenRajapvm(params.get("ensikertalaisuudenRajapvm"))
    new AsyncResult() {
      override implicit def timeout: Duration = 120.seconds
      override val is =
        (ensikertalainenActor ? EnsikertalainenQuery(personOids, paivamaara = rajapvm))(120.seconds).mapTo[Seq[Ensikertalainen]]
    }
  }

  incident {
    case t: ParamMissingException => (id) => BadRequest(IncidentReport(id, t.getMessage))
    case t: ExecutionException => (id) => InternalServerError(IncidentReport(id, "backend service failed"))
    case t: PreconditionFailedException => (id) => InternalServerError(IncidentReport(id, "backend service failed"))
  }

}

