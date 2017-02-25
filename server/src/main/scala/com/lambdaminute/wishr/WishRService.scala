package com.lambdaminute

import java.io.File

import com.lambdaminute.wishr.WishRAuthentication
import com.lambdaminute.wishr.config.ApplicationConf
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.notification.Email
import com.lambdaminute.wishr.persistence.Persistence
import fs2.Task
import io.circe.Printer
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceInstances
import org.http4s.dsl._
import org.http4s.server.syntax._
import cats.implicits._

case class WishRService(persistence: Persistence[String, String],
                        authentication: WishRAuthentication,
                        configuration: ApplicationConf)
    extends CirceInstances {

  override protected def defaultPrinter: Printer = Printer.spaces2

  def serveFile(path: String, request: Request) =
    StaticFile
      .fromFile(new File(path), Some(request))
      .map(Task.now)
      .getOrElse(NotFound())

  def service: Service[Request, MaybeResponse] =
    unauthedService orElse authentication.middleware(authedService)

  val unauthedService: HttpService = HttpService {

    case request @ (GET -> Root) =>
      println(s"Got request for root")
      serveFile("./index.html" + request.pathInfo, request)

    case request
        if request.method == GET && List(".css", ".html", ".js", ".ico").exists(
          request.pathInfo.endsWith) =>
      println(s"Got static file request: ${request.pathInfo}")
      serveFile("." + request.pathInfo, request)

    case request @ (POST -> Root / "createuser") =>
      val createUserRequest = request.as(jsonOf[CreateUserRequest]).unsafeRun()
      val registrationToken = java.util.UUID.randomUUID.toString
      println(s"""Creating ${createUserRequest.copy(password = "")}""")

      val token = java.util.UUID.randomUUID.toString
      val userCreated =
        persistence.createUser(createUserRequest, token)

      userCreated match {
        case Left(err) => BadRequest(err)
        case Right(s) =>
          println(s"Sending activation email to ${createUserRequest.email}")
          sendActivationEmail(createUserRequest.email, token)
          Ok(s)
      }
    case GET -> Root / finalize / token =>
      persistence.finalize(token) match {
        case Left(err) => BadRequest(err)
        case Right(s)  => Ok(s)
      }

  }

  private def sendActivationEmail(email: String, token: String): String =
    Email(configuration.emailSettings, configuration.rootPath)
      .sendTo(email, token)

  def authedService: AuthedService[User] = AuthedService {

    case request @ (POST -> Root / "login" as user) =>
      println("LOGIN REQUEST"+request)
      Ok(user.secret)

    case GET -> Root / "entries" as user =>
      getEntriesFor(user.name)

    case request @ (POST -> Root / "set" as user) =>
      val wishes: List[Wish] = request.req.as(jsonOf[List[Wish]]).unsafeRun()
      setWishesFor(wishes, user)

  }

  private def getEntriesFor(username: String) = {
    val entries: Either[String, List[WishEntry]] = persistence.getEntriesFor(username)
    println(s"Getting entries for ${username}")

    val wishes: Either[String, List[Wish]] = entries.map {
      case actualEntries =>
        actualEntries.map {
          case WishEntry(_, heading, desc, image, id) => Wish(heading, desc, Option(image))
        }
    }

    println(s"Found wishes $wishes")

    wishes match {
      case Right(wishes) => Ok(wishes)(jsonEncoderOf[List[Wish]])
      case Left(error)   => Ok(error)
    }
  }

  private def setWishesFor(wishes: List[Wish], user: User) = {

    val entries = wishes.map {
      case Wish(heading, desc, image) =>
        WishEntry(user.name, heading, desc, image.getOrElse(""), -1)
    }

    println(s"Setting wishes for $user: ${wishes.mkString}")

    persistence.set(entries) match {
      case Right(addResult) => Ok(addResult)
      case Left(err)        => InternalServerError(err)
    }
  }
}
