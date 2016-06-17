package fi.vm.sade.hakurekisteri.storage


import java.io.{File, PrintWriter}

import slick.driver.PostgresDriver.api._

object SchemaGenerator extends App {
  println("generating schema.ddl...")

  val statements = HakurekisteriTables.allTables.map(_.schema).reduce(_ ++ _).createStatements.mkString(";\n\n") + ";\n"

  val writer = new PrintWriter(new File("db/schema.ddl"))
  writer.write(statements)
  writer.close()

  println("schema.ddl generated")
}
