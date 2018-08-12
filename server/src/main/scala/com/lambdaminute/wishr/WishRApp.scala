package com.lambdaminute.wishr

import cats.effect._
import ciris._
import com.lambdaminute.WishRService
import com.lambdaminute.wishr.config.{ApplicationConf, DBConfig}
import com.lambdaminute.wishr.persistence.DoobiePersistence
import fs2.StreamApp.ExitCode
import fs2.{StreamApp, _}
import org.http4s.server.blaze.BlazeBuilder
import cats.syntax.either._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

abstract class WishRApp extends StreamApp[IO] {

  private def parseDbUrl(url: String): Either[ConfigErrors, DBConfig] =
    try {
      val colonSplit  = url.split(":")
      val driver      = colonSplit(0)
      val user        = colonSplit(1).filter(_ != '/')
      val password    = colonSplit(2).takeWhile(_ != '@')
      val filteredUrl = url.dropWhile(_ != '@').filter(_ != '@')
      val dBConfig    = DBConfig(user, password, filteredUrl, driver)
      println(dBConfig)
      Either.right(dBConfig)
    } catch {
      case t: Throwable => Either.left(ConfigErrors.apply(ConfigError(t.getMessage)))
    }

  def loadDbConf: Either[ConfigErrors, DBConfig] =
    loadConfig(env[Option[String]]("DATABASE_URL"))(
      _.getOrElse("postgres://pg:password@localhost:5432"))
      .flatMap(parseDbUrl)

  def loadAppConf: Either[ConfigErrors, ApplicationConf] =
    for {
      dbConf     <- loadDbConf
      staticPath <- loadStaticPath
      port       <- loadConfig(env[Option[Int]]("PORT"))(_.getOrElse(9000))
    } yield ApplicationConf(dbConf, staticPath, port)

  def loadStaticPath: Either[ConfigErrors, String] =
    loadConfig(env[Option[String]]("STATIC_PATH"))(_.getOrElse("/static/"))

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
      persistence = new DoobiePersistence[IO](applicationConf.dbconf, 50.minutes)
      wishrService <- Stream.eval(
        IO.pure(
          new WishRService[IO](applicationConf,
                               persistence,
                               token => new Authed(token, persistence),
                               new Unauthed(persistence))))
      result <- BlazeBuilder[IO]
        .bindHttp(port = applicationConf.port, host = "0.0.0.0")
        .mountService(wishrService.unauthedService, "/api/")
        .mountService(wishrService.wrappedAuthedService, "/authed/api/")
        .mountService(wishrService.staticFilesService(applicationConf.staticPath), "/")
        .serve
    } yield result

}
