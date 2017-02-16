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
import fs2.interop.cats._

import org.http4s.dsl._

import cats.implicits._
import cats._

import org.http4s.server.syntax._

case class WishRService(persistence: Persistence[String, String], authentication: WishRAuthentication)
    extends CirceInstances {

  override protected def defaultPrinter: Printer = Printer.spaces2

  def serveFile(path: String, request: Request) =
    StaticFile
      .fromFile(new File(path), Some(request))
      .map(Task.now)
      .getOrElse(NotFound())

  def service: Service[Request, MaybeResponse] = unauthedService orElse authentication.middleware(authedService)

  val unauthedService: HttpService = HttpService {
    case request
        if request.method == GET && List(".css", ".html", ".js", ".ico").exists(request.pathInfo.endsWith) =>
      println(s"Got static file request: ${request.pathInfo}")
      serveFile("." + request.pathInfo, request)
  }

  def authedService: AuthedService[User] = AuthedService {

    case request @ (POST -> Root / "login" as user) => Ok(user.secret)

    case GET -> Root / "entries" as user =>
      val entries: Either[String, List[WishEntry]] = persistence.getEntriesFor(user.name)

      val wishes: Either[String, List[Wish]] = entries.map {
        case actualEntries =>
          actualEntries.map {
            case WishEntry(_, heading, desc, image) => Wish(heading, desc, image)
          }
      }

      wishes match {
        case Right(wishes) => Ok(wishes)(jsonEncoderOf[List[Wish]])
        case Left(error)   => Ok(error)
      }

    case request @ (POST -> Root / "set" as user) =>
      val wishes: List[Wish] = request.req.as(jsonOf[List[Wish]]).unsafeRun()

      val entries = wishes.map {
        case Wish(heading, desc, image) =>
          WishEntry(user.name, heading, desc, image)
      }

      println(s"Setting wishes for $user: ${wishes.mkString}")

      persistence.set(entries) match {
        case Right(addResult) => Ok(addResult)
        case Left(err)        => InternalServerError(err)
      }

  }

}
