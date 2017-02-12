package com.lambdaminute

import org.http4s._
import org.http4s.server._
import org.http4s.dsl._
import _root_.argonaut._
import Argonaut._
import org.http4s.argonaut._
import io.getquill._
import cats._
import cats.data.EitherT
import cats.syntax.either._
import com.lambdaminute.wishr.model.WishList
import io.circe.Printer
import org.http4s.circe.CirceInstances

import scala.util.Either

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

object HelloWorld extends CirceInstances {

  def ctx = new SqlMirrorContext[MirrorSqlDialect, Literal]

//  def jsonDecoder[T]: EntityDecoder[T] =
//    EntityDecoder.decodeBy(MediaType.`application/json`) { msg =>
//      EitherT {
//        msg.as[String].map(s => Either.right(s))
//      }
//    }



  def service = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name! What a wonderful day ;)")
    case request @ (POST -> Root / "add") =>
      println(s"unsafe: ${request.as[String].unsafeRun()}")
      println(s"as object: ${request.as(jsonOf[WishList]).unsafeRun()}")
      Ok(s"Nice")
  }

  override protected def defaultPrinter: Printer = io.circe.Printer(preserveOrder = true, true, "  ")
}
