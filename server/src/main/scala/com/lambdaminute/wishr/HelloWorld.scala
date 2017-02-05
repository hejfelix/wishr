package com.lambdaminute

import org.http4s._
import org.http4s.server._
import org.http4s.dsl._

import _root_.argonaut._, Argonaut._
import org.http4s.argonaut._

object HelloWorld {
  val service = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name! What a wonderful day ;)")
  }
}
