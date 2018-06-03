package com.lambdaminute.wishr

import cats.data.EitherT
import cats.effect._
import cats.implicits._
import ciris._
import com.lambdaminute.WishRService
import com.lambdaminute.wishr.config.{ApplicationConf, DBConfig}
import com.lambdaminute.wishr.model.tags._
import com.lambdaminute.wishr.model.{UnauthedApi, UserInfo, Wish}
import com.lambdaminute.wishr.persistence.{DoobiePersistence, Persistence}
import fs2.StreamApp.ExitCode
import fs2.{StreamApp, _}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.middleware.CORS

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

abstract class WishRApp extends StreamApp[IO] {

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
        .getOrElse(DBConfig("pg", "password", "localhost:5432", "jdbc:postgresql")))

  def loadAppConf: Either[ConfigErrors, ApplicationConf] =
    for {
      dbConf <- loadDbConf
    } yield ApplicationConf(dbConf)

  def loadConfOrExit: IO[ApplicationConf] =
    IO.pure(loadAppConf match {
      case Right(appConf) => appConf
      case Left(errs) =>
        sys.error(s"""Failed to load configuration:${errs.messages.mkString("\n")}""".stripMargin)
        sys.exit(1)
    })

  println("Starting...")
  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    for {
      applicationConf <- Stream.eval(loadConfOrExit)
//      numMigrations   <- Stream.eval(db.init(applicationConf.dbconf))
//     _ = println(numMigrations)
      persistence = new DoobiePersistence[IO](applicationConf.dbconf, 50.minutes)
      wishrService <- Stream.eval(
        IO.pure(
          new WishRService[IO](applicationConf,
                               persistence,
                               token => new Authed(token, persistence),
                               new Unauthed(persistence))))
      result <- BlazeBuilder[IO]
        .bindHttp(port = 9000, host = "0.0.0.0")
        .mountService(wishrService.unauthedService, "/api/")
        .mountService(wishrService.wrappedAuthedService, "/authed/api/")
        .serve
    } yield result

}
