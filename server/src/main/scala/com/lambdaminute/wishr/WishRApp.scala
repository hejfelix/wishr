package com.lambdaminute.wishr

import cats.effect._
import cats.implicits._
import ciris._
import com.lambdaminute.WishRService
import com.lambdaminute.wishr.config.{ApplicationConf, DBConfig}
import com.lambdaminute.wishr.persistence.DoobiePersistence
import fs2.StreamApp.ExitCode
import fs2.{StreamApp, _}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.middleware.CORS

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class WishRApp[F[_]](implicit F: Effect[F]) extends StreamApp[F] {

  private def parseDbUrl(url: String): DBConfig = {
    val driver      = url.takeWhile(_ != ':').mkString
    val user        = url.split("//").drop(1).head.takeWhile(_ != ':').mkString
    val password    = url.split(":").drop(2).head.takeWhile(_ != '@').mkString
    val filteredUrl = url.split("@").drop(1).mkString.split("/").dropRight(1).mkString
    println(filteredUrl)
    DBConfig(user, password, filteredUrl, driver)
  }

  def loadDbConf: Either[ConfigErrors, DBConfig] =
    Right(
      loadConfig(env[String]("DB_URL"))(parseDbUrl)
        .getOrElse(DBConfig("postgres", "password", "localhost:5432", "jdbc:postgresql")))

  def loadAppConf: Either[ConfigErrors, ApplicationConf] =
    for {
      dbConf <- loadDbConf
    } yield ApplicationConf(dbConf)

  def loadConfOrExit: F[ApplicationConf] =
    F.delay(loadAppConf match {
      case Right(appConf) => appConf
      case Left(errs) =>
        sys.error(s"""Failed to load configuration:${errs.messages.mkString("\n")}""".stripMargin)
        sys.exit(1)
    })

  println("Starting...")
  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    for {
      applicationConf <- Stream.eval(loadConfOrExit)
//      numMigrations   <- Stream.eval(db.init(applicationConf.dbconf))
//     _ = println(numMigrations)
      wishrService <- Stream.eval(
        F.delay(new WishRService[F](applicationConf,
                                    new DoobiePersistence[F](applicationConf.dbconf, 50.minutes))))
      result <- BlazeBuilder[F]
        .bindHttp(port = 9000)
        .mountService(CORS(wishrService.service))
        .serve
    } yield result

}
