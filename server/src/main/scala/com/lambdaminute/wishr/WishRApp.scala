package com.lambdaminute.wishr

import cats.effect._
import ciris._
import com.lambdaminute.WishRService
import com.lambdaminute.wishr.config.{ApplicationConf, DBConfig}
import fs2._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.middleware.CORS
import fs2.StreamApp
import fs2.StreamApp.ExitCode

class WishRApp[F[_]](implicit F: Effect[F]) extends StreamApp[F] {

  private def parseDbUrl(url: String): DBConfig = ???

  def loadDbConf: Either[ConfigErrors, DBConfig] = loadConfig(env[String]("DB_URL"))(parseDbUrl)

  def loadAppConf: Either[ConfigErrors, ApplicationConf] =
    for {
      dbConf <- loadDbConf
    } yield ApplicationConf(dbConf)

  def loadConfOrExit: F[ApplicationConf] =
    F.delay(loadAppConf match {
      case Right(appConf) => appConf
      case Left(errs) =>
        sys.error("Failed to load configuration:\n" + errs.messages.mkString("\n"))
        sys.exit(1)
    })

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    for {
      applicationConf <- Stream.eval(loadConfOrExit)
      _               <- Stream.eval(db.init(applicationConf.dbconf))
      wishrService    <- Stream.eval(F.delay(WishRService[F](applicationConf)))
      server <- BlazeBuilder[F]
        .bindHttp()
        .mountService(CORS(wishrService.service))
        .serve
    } yield server

}
