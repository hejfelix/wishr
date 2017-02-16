package com.lambdaminute

import java.io.File

import cats.arrow.Choice
import cats.data.Kleisli
import com.lambdaminute.wishr.WishRAuthentication
import com.lambdaminute.wishr.model.{LoginRequest, User, Wish, WishEntry}
import com.lambdaminute.wishr.persistence.Persistence
import fs2.Task
import io.circe.{Encoder, Printer}
import io.circe.generic.auto._
import org.http4s.circe.CirceInstances
import org.http4s.server.AuthMiddleware
import org.http4s._
// import org.http4s._
import fs2.interop.cats._

import org.http4s.dsl._
// import org.http4s.dsl._

import cats.implicits._
import cats._

import org.http4s.server._

case class WishRService(persistence: Persistence, authentication: WishRAuthentication) extends CirceInstances {

  override protected def defaultPrinter: Printer = Printer.spaces2

  def serveFile(path: String, request: AuthedRequest[User]) =
    StaticFile
      .fromFile(new File(path), Some(request.req))
      .map(Task.now)
      .getOrElse(NotFound())

  def basicAuthService: Service[Request, MaybeResponse] = authentication.middleware(authedService)

  def authedService: AuthedService[User] = AuthedService {

    case request @ (GET -> Root as user) =>
      println("Serving index.html")
      serveFile("./index.html", request)

    case GET -> Root / "entries" as user =>
      val entries = persistence.getEntriesFor(user.name)

      val wishes: List[Wish] = entries.map {
        case WishEntry(_, heading, desc, image) =>
          Wish(heading, desc, image)
      }

      Ok(wishes)(jsonEncoderOf[List[Wish]])

    case request @ (POST -> Root / "set" as user) =>
      val wishes: List[Wish] = request.req.as(jsonOf[List[Wish]]).unsafeRun()

      val entries = wishes.map {
        case Wish(heading, desc, image) =>
          WishEntry(user.name, heading, desc, image)
      }

      println(s"Setting wishes for $user: ${wishes.mkString}")

      val addResult: String = persistence.set(entries)
      Ok(addResult)

    case request @ (_ as user)
        if request.req.method == GET && List(".css", ".html", ".js", ".ico").exists(request.req.pathInfo.endsWith) =>
      println(s"Got static file request: ${request.req.pathInfo}")
      serveFile("." + request.req.pathInfo, request)
  }

}
