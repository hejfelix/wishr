package com.lambdaminute

import com.lambdaminute.wishr.persistence.{DictionaryPersistence, QuillPersistence}
import fs2.Task
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._

object BlazeExample extends ServerApp {

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(WishRService(new DictionaryPersistence()).service, "/")
      .start

}
