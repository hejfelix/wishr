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
import org.http4s.twirl._

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
    denyNonSSLService orElse unauthedService orElse authentication.middleware(authedService)

  val denyNonSSLService: HttpService = HttpService {
    case request
        if request.headers
          .get(headers.`X-Forwarded-Proto`)
          .map(_.value)
          .getOrElse("") != "https" =>
      Uri.fromString(configuration.rootPath) match {
        case Right(uri)                             => TemporaryRedirect(uri)
        case Left(ParseFailure(sanitized, details)) => InternalServerError(s"$sanitized, $details")
      }
  }

  val unauthedService: HttpService = HttpService {

    case GET -> Root / "status" =>
      Ok()

    case GET -> Root / "stats" =>
      persistence.getStats().value.flatMap {
        case Right(stats) => Ok(stats)(jsonEncoderOf[Stats])
        case Left(err)    => InternalServerError(err)
      }

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
      val secretURL                                      = request.params.get("sharedURL").get
      val owner: persistence.PersistenceResponse[String] = persistence.userForSecretURL(secretURL)

      println(s"Fetching wishes for secret $secretURL")

      owner.value.flatMap {
        case Right(owner) =>
          Ok(html.share(secretURL, owner))
        case Left(err) => NotFound(err)
      }

    case request @ (GET -> Root) =>
      println(s"Got request for root")
      Ok(html.index())

    case request
        if request.method == GET && (List(".css", ".html", ".js", ".ico", ".svg").exists(
          request.pathInfo.endsWith) || request.pathInfo.contains("acme-challenge")) =>
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

    case request @ POST -> Root / "grant" as user =>
      val wish: Task[Wish] = request.req.as(jsonOf[Wish])
      println(s"Granting wish ${wish} for user ${user}")

      wish
        .map {
          case Wish(heading, desc, image) =>
            WishEntry(user.name, heading, desc, image.mkString, 0, -1)
        }
        .flatMap(w => persistence.grant(w).value)
        .flatMap {
          case Right(addResult) => println("WISH GRANTED"); Ok(addResult)
          case Left(err)        => println(s"WISH NOT GRANTED $err"); InternalServerError(err)
        }
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
