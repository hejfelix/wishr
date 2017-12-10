package flywaycodegen

import slick.jdbc.{JdbcProfile, PostgresProfile}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await

object FlywayCodeGen {

  def main(args: Array[String]): Unit = {
    val profileName = args(0)
    val jdbcDriver  = args(1)
    val dbUrl       = args(2)
    val genPath     = args(3)
    val packageName = args(4)
    val user        = args(5)
    val password    = args(6)

    val dbProfile: JdbcProfile = PostgresProfile

    val db =
      dbProfile.api.Database.forURL(dbUrl, user = user, password = password, driver = jdbcDriver)
    val excludedTables = List("schema_version")

    val codegen = db
      .run {
        dbProfile.defaultTables
          .map(_.filter(table => !excludedTables.contains(table.name.name)))
          .flatMap(dbProfile.createModelBuilder(_, false).buildModel)
      }
      .map(model => new slick.codegen.SourceCodeGenerator(model))

    val prefixedCodegen =
      codegen.map(generator => {
        val str = generator.packageCode(profileName, packageName, "Tables", None)
        println(str)
        generator.writeStringToFile(s"//scalastyle:off\n$str", genPath, packageName, "Tables.scala")
      })

    Await.ready(
      prefixedCodegen,
      20.seconds
    )
  }

}
