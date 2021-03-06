package support

import java.text.SimpleDateFormat
import java.util.Date

import fi.vm.sade.hakurekisteri.integration.ytl.YtlIntegration
import org.apache.commons.lang3.time.DateUtils
import org.quartz.CronExpression
import org.slf4j.LoggerFactory

object YtlRerunPolicy {
  private val logger = LoggerFactory.getLogger(YtlRerunPolicy.getClass)

  def rerunPolicy(expression: String, ytlIntegration: YtlIntegration): () => Unit = {
    def nextTimestamp(expression: String, d: Date) = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new CronExpression(expression).getNextValidTimeAfter(d))
    logger.info(s"First YTL fetch at '${nextTimestamp(expression, new Date())}'")

    () => {
      val fetchStatus = ytlIntegration.getLastFetchStatus
      val isRunning = fetchStatus.exists(_.inProgress)
      if(isRunning) {
        logger.info(s"Scheduled to make YTL fetch but fetch is already running! Next try will be ${nextTimestamp(expression, new Date())}")
      } else {
        val isYesterday = fetchStatus.exists(status => !DateUtils.isSameDay(status.start, new Date()))
        val isSucceeded = fetchStatus.flatMap(_.succeeded).getOrElse(false)
        if((isSucceeded && isYesterday) || (!isSucceeded)) {
          logger.info(s"Starting new YTL fetch because: last run was yesterday=$isYesterday and that run succeeded=$isSucceeded")
          ytlIntegration.syncAll()
        } else {
          logger.info(s"Scheduled to make YTL fetch but not running because: " +
            s"last run was yesterday=$isYesterday and that run succeeded=$isSucceeded! " +
            s"Next try will be ${nextTimestamp(expression, new Date())}")
        }
      }
    }
  }

}
