package com.lambdaminute

import java.io.File

import cats.data.EitherT
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
import fs2.interop.cats._

case class WishRService(persistence: Persistence[String, String],
                        authentication: WishRAuthentication,
                        configuration: ApplicationConf)
    extends CirceInstances {

  override protected def defaultPrinter: Printer = Printer.spaces2

  def serveFile(path: String, request: Request): Task[Response] =
    StaticFile
      .fromFile(new File(path), Some(request))
      .map(Task.now)
      .getOrElse(NotFound())

  def service: Service[Request, MaybeResponse] =
    unauthedService orElse authentication.middleware(authedService)

  val unauthedService: HttpService = HttpService {

    case GET -> Root / "status" =>
      Ok()

    case GET -> Root / "shared-wishes" / secretURL =>
      println(s"Fetching shared wishes for url $secretURL")
      val entries: EitherT[Task, String, List[WishEntry]] = for {
        email   <- persistence.emailForSecretURL(secretURL)
        entries <- persistence.getEntriesFor(email)
      } yield entries

      entries.value.flatMap {
        case Right(wishes) => Ok(wishes)(jsonEncoderOf[List[WishEntry]])
        case Left(error)   => Ok(error)
      }
    case request @ GET -> Root if request.params.contains("sharedURL") =>
      val secretURL = request.params.get("sharedURL").get
      println(s"Fetching wishes for secret $secretURL")
      serveFile("index-shared.html", request)
        .map(_.addCookie(Cookie("secretURL", secretURL)))
    case request @ (GET -> Root) =>
      println(s"Got request for root")
      serveFile("./index.html" + request.pathInfo, request)

    case request
        if request.method == GET && (List(".css", ".html", ".js", ".ico", ".svg").exists(
          request.pathInfo.endsWith) || request.pathInfo.contains("acme-challenge"))=>
      println(s"Got static file request: ${request.pathInfo}")
      serveFile("." + request.pathInfo, request)

    case request @ (POST -> Root / "createuser") =>
      val createUserRequest = request.as(jsonOf[CreateUserRequest]).unsafeRun()
      val registrationToken = java.util.UUID.randomUUID.toString
      println(s"""Creating ${createUserRequest.copy(password = "")}""")

      val token = java.util.UUID.randomUUID.toString
      val userCreated: Task[Either[String, String]] =
        persistence.createUser(createUserRequest, token).value

      userCreated.flatMap {
        case Left(err) => BadRequest(err)
        case Right(s) =>
          println(s"Sending activation email to ${createUserRequest.email}")
          sendActivationEmail(createUserRequest.email, token)
          Ok(s)
      }

    case GET -> Root / "finalize" / token =>
      persistence.finalize(token).value.flatMap {
        case Left(err) => BadRequest(err)
        case Right(s)  => Ok(s)
      }

  }

  private def sendActivationEmail(email: String, token: String): String =
    Email(configuration.emailSettings, configuration.rootPath)
      .sendTo(email, token)

  def authedService: AuthedService[User] = AuthedService {

    case request @ GET -> Root / "sharingURL" as user =>
      persistence.getSharingURL(user.name).value.flatMap {
        case Right(secret) =>
          val url = s"${configuration.rootPath}/?sharedURL=$secret"
          Ok(url)
        case Left(err) => NotFound(err)
      }

    case request @ POST -> Root / "login" as user =>
      println("LOGIN REQUEST" + user)
      Ok(user.secret)
        .putHeaders(Header("Set-Cookie", s"authsecret=${user.secret}; Max-Age=1337"),
                    Header("Set-Cookie", s"authname=${user.name}; Max-Age=1337"))

    case GET -> Root / "entries" as user =>
      getEntriesFor(user.name)

    case request @ POST -> Root / "set" as user =>
      val wishes: Task[List[Wish]] = request.req.as(jsonOf[List[Wish]])

      wishes.flatMap(w => setWishesFor(w, user))
  }

  private def getEntriesFor(username: String) = {
    val entries: persistence.PersistenceResponse[List[WishEntry]] =
      persistence.getEntriesFor(username)
    println(s"Getting entries for ${username}")

    val wishes: EitherT[Task, String, List[Wish]] = entries.map {
      _.map {
        case WishEntry(_, heading, desc, image, index, id) => Wish(heading, desc, Option(image))
      }
    }

    println(s"Found wishes $wishes")

    wishes.value.flatMap {
      case Right(wishes) => Ok(wishes)(jsonEncoderOf[List[Wish]])
      case Left(error)   => Ok(error)
    }
  }

  private def setWishesFor(wishes: List[Wish], user: User) = {

    val entries: List[WishEntry] = wishes.zipWithIndex.map {
      case (Wish(heading, desc, image), index) =>
        WishEntry(user.name, heading, desc, image.getOrElse(""), index, -1)
    }

    println(s"Setting wishes for $user: ${wishes.mkString}")

    persistence.set(entries, user.name).value.flatMap {
      case Right(addResult) => Ok(addResult)
      case Left(err)        => InternalServerError(err)
    }
  }
}
