package mocha

import java.io.IOException
import java.net.Socket

import fi.vm.sade.hakurekisteri.JettyTestLauncher
import org.scalatest.{FlatSpec, Matchers}
import org.scalatra.{ScalatraContext, ScalatraBase}

import scala.language.postfixOps
import scala.sys.process._
import scala.util.Random

class HakureRekisteriMochaTest extends FlatSpec with Matchers {

  def isFreeLocalPort(port: Int): Boolean = {
    try {
      val socket = new Socket("127.0.0.1", port)
      socket.close()
      false
    } catch {
      case e: IOException => true
    }
  }

  def findFreeLocalPort: Int = {
    val range = 1024 to 60000
    val port = ((range(new Random().nextInt(range length))))
    if (isFreeLocalPort(port)) {
      port
    } else {
      findFreeLocalPort
    }
  }

  "Mocha tests" should "pass" in {
    val jettyPort: Int = findFreeLocalPort
    new JettyTestLauncher(jettyPort).withJetty {
      val pb = Seq("node_modules/mocha-phantomjs/bin/mocha-phantomjs", "-R", "spec", "http://localhost:" + jettyPort + "/test/runner.html")
      val res = pb.!
      res should be(0)
    }
  }

}
