package fi.vm.sade.hakurekisteri.tools

import java.io.InputStream
import java.util.zip.ZipInputStream

import scala.util.Try

object Zip {

  def toInputStreams(z: ZipInputStream): Iterator[InputStream] = {
    Iterator.continually(Try(z.getNextEntry()).getOrElse(null))
      .takeWhile(_ != null)
      .map(_ => z)
  }

}