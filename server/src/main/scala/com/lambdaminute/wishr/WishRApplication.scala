package com.lambdaminute.wishr

import com.lambdaminute.WishRService
import com.lambdaminute.wishr.config.Module.ModuleOr
import com.lambdaminute.wishr.config.{
  ApplicationConf,
  PersistenceConfig,
  PersistenceModule,
  WeakPersistenceConfig
}
import com.lambdaminute.wishr.persistence.Persistence
import fs2.Task
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}
import pureconfig._

import scala.util.Try

object WishRApplication extends ServerApp {

  val configuration: ApplicationConf = loadConfig[ApplicationConf].get

  println(configuration)

  val persistenceModule = new PersistenceModule()
  val persistence: ModuleOr[Persistence[String, String]] =
    persistenceModule.fromConfig.run(WeakPersistenceConfig("someUser", "somePassword"))

  val serviceTask = persistence match {
    case Right(persistence) =>
      Task.now(WishRService(persistence, WishRAuthentication(persistence), configuration))
    case Left(err) => Task.fail(new Exception(err))
  }

  override def server(args: List[String]): Task[Server] =
    serviceTask.flatMap(
      wishrService =>
        BlazeBuilder
          .bindHttp(8080, "localhost")
          .mountService(wishrService.service, "/")
          .start)

}
