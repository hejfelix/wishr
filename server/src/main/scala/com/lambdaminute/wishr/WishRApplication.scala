package com.lambdaminute.wishr

import com.lambdaminute.WishRService
import com.lambdaminute.wishr.config.Module.ModuleOr
import com.lambdaminute.wishr.config._
import com.lambdaminute.wishr.persistence.Persistence
import fs2.Task
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}
import pureconfig._

object WishRApplication extends ServerApp {

  //How to use this with ServerApp, since it expects Task[Server] and no Try[...]
  val configuration: ApplicationConf = loadConfig[ApplicationConf]("wishrSettings").get
//  val dbConf                         = DBConfig.fromStringUrl(configuration.dburl)

  val persistenceModule                                   = new PersistenceModule()
  private val dbConfig: PersistenceConfig[String, String] = WeakPersistenceConfig("", "")
  val persistence: ModuleOr[Persistence[String, String]] =
    persistenceModule.fromConfig.run(dbConfig)

  val serviceTask = persistence match {
    case Right(persistence) =>
      Task.now(WishRService(persistence, WishRAuthentication(persistence), configuration))
    case Left(err) => Task.fail(new Exception(err))
  }

  override def server(args: List[String]): Task[Server] =
    serviceTask.flatMap(
      wishrService =>
        BlazeBuilder
          .bindHttp(configuration.port, "0.0.0.0")
          .mountService(wishrService.service, "/")
          .start
    )

}
