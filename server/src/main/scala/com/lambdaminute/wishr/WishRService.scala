package com.lambdaminute

import java.io.File

import cats.data.Kleisli
import com.lambdaminute.wishr.model.{User, Wish, WishEntry}
import com.lambdaminute.wishr.persistence.Persistence
import fs2.Task
import io.circe.Printer
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceInstances
import org.http4s.dsl._
import org.http4s.server.AuthMiddleware

case class WishRService(persistence: Persistence) extends CirceInstances {

  override protected def defaultPrinter: Printer = Printer.spaces2

  def serveFile(path: String, request: Request) =
    StaticFile
      .fromFile(new File(path), Some(request))
      .map(Task.now) // This one is require to make the types match up
      .getOrElse(NotFound()) // In case the file doesn't exist

  //Authenticate the user
  def authUser: Service[Request, User] = Kleisli(_ => Task.delay(???))

  def middleware = AuthMiddleware(authUser)

  val authedService: AuthedService[User] =
    AuthedService {
      case GET -> Root / "welcome" as user => Ok(s"Welcome, ${user.name}")
    }

  def basicAuthService: Kleisli[Task, Request, MaybeResponse] =
    middleware(authedService)

  import org.http4s.server.syntax._
  def service = noAuthService orElse basicAuthService

  def noAuthService: Kleisli[Task, Request, MaybeResponse] = HttpService {

    case request @ (GET -> Root) =>
      println("Serving index.html")
      serveFile("./index.html", request)

    case GET -> Root / user / "entries" =>
      val entries = persistence.getEntriesFor(user)

      val wishes: List[Wish] = entries.map {
        case WishEntry(_, heading, desc, image) =>
          Wish(heading, desc, image)
      }

      Ok(wishes)(jsonEncoderOf[List[Wish]])

    case request @ (POST -> Root / user / "set") =>
      val wishes: List[Wish] = request.as(jsonOf[List[Wish]]).unsafeRun()

      val entries = wishes.map {
        case Wish(heading, desc, image) =>
          WishEntry(user, heading, desc, image)
      }

      println(s"Setting wishes for $user: ${wishes.mkString}")

      val addResult: String = persistence.set(entries)
      Ok(addResult)

    case request
        if request.method == GET && List(".css", ".html", ".js").exists(
          request.pathInfo.endsWith) =>
      println(s"Got static file request: ${request.pathInfo}")
      serveFile("." + request.pathInfo, request)
  }

}
