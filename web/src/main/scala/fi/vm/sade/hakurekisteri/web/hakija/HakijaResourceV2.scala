package fi.vm.sade.hakurekisteri.web.hakija

import java.io.OutputStream

import _root_.akka.actor.{ActorRef, ActorSystem}
import _root_.akka.event.{Logging, LoggingAdapter}
import _root_.akka.pattern.{AskTimeoutException, ask}
import _root_.akka.util.Timeout
import fi.vm.sade.hakurekisteri.hakija._
import fi.vm.sade.hakurekisteri.rest.support._
import fi.vm.sade.hakurekisteri.web.HakuJaValintarekisteriStack
import fi.vm.sade.hakurekisteri.web.rest.support.ApiFormat.ApiFormat
import fi.vm.sade.hakurekisteri.web.rest.support.{ApiFormat, IncidentReport, _}
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerEngine}

import scala.compat.Platform
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.Try

class HakijaResourceV2(hakijaActor: ActorRef)
                      (implicit system: ActorSystem, sw: Swagger, val security: Security, val ct: ClassTag[JSONHakijat])
  extends HakuJaValintarekisteriStack with HakijaSwaggerApi with HakurekisteriJsonSupport with JacksonJsonSupport with FutureSupport with SecuritySupport with ExcelSupport[JSONHakijat] with DownloadSupport with QueryLogging {

  override protected implicit def executor: ExecutionContext = system.dispatcher

  override protected def applicationDescription: String = "Hakijatietojen rajapinta"

  override protected implicit def swagger: SwaggerEngine[_] = sw

  implicit val defaultTimeout: Timeout = 120.seconds
  override val logger: LoggingAdapter = Logging.getLogger(system, this)

  def getContentType(t: ApiFormat): String = t match {
    case ApiFormat.Json => formats("json")
    case ApiFormat.Excel => formats("binary")
    case tyyppi => throw new IllegalArgumentException(s"tyyppi $tyyppi is not supported")
  }

  override protected def renderPipeline: RenderPipeline = renderExcel orElse super.renderPipeline

  override val streamingRender: (OutputStream, JSONHakijat) => Unit = (out, hakijat) => {
    ExcelUtilV2.write(out, hakijat)
  }

  get("/", operation(queryV2)) {
    if(params.get("haku").getOrElse("").isEmpty)
      throw new IllegalArgumentException(s"Haku can not be empty")
    val t0 = Platform.currentTime
    val q = HakijaQuery(params, currentUser, 2)

    val tyyppi = Try(ApiFormat.withName(params("tyyppi"))).getOrElse(ApiFormat.Json)
    contentType = getContentType(tyyppi)

    new AsyncResult() {
      override implicit def timeout: Duration = 120.seconds

      val hakuResult = hakijaActor ? q

      val hakijatFuture = hakuResult.flatMap {
        case result if Try(params("tiedosto").toBoolean).getOrElse(false) || tyyppi == ApiFormat.Excel =>
          setContentDisposition(tyyppi, response, "hakijat")
          Future.successful(result)
        case result =>
          Future.successful(result)
      }

      logQuery(q, t0, hakijatFuture)

      val is = hakijatFuture
    }
  }

  incident {
    case t: AskTimeoutException => (id) => InternalServerError(IncidentReport(id, "back-end service timed out"))
  }
}



