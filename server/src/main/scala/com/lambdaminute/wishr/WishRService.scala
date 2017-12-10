package com.lambdaminute

import java.io.File

import cats.effect.Effect
import com.lambdaminute.wishr.config.ApplicationConf
import com.lambdaminute.wishr.model.Api
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpService, MediaType, Request, StaticFile}
import org.http4s.circe._
import concurrent.duration._
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

object MyServer extends autowire.Server[String, Decoder, Encoder] {

  def read[Result](p: String)(implicit evidence$1: Decoder[Result]): Result =
    decode[Result](p).right.get

  def write[Result](r: Result)(implicit evidence$2: Encoder[Result]): String =
    r.asJson.spaces2

}

object Template {
  import scalatags.Text.all._
  import scalatags.Text.tags2.title

  val txt =
    "<!DOCTYPE html>" +
      html(
        head(
          title("Example Scala.js application"),
          meta(httpEquiv := "Content-Type", content := "text/html; charset=UTF-8"),
          script(`type` := "text/javascript", src := "/client-fastopt.js"),
          script(`type` := "text/javascript", src := "http://localhost:12345/workbench.js")
        ),
        body(margin := 0)(
          script("Main.main")
        )
      )
}

case class WishRService[F[_]](applicationConf: ApplicationConf)(implicit F: Effect[F])
    extends Http4sDsl[F]
    with Api {

  def static(file: String, request: Request[F]) =
    StaticFile.fromResource("/" + file, Some(request))

  def service: HttpService[F] = HttpService[F] {
    case GET -> Root =>
      Ok(Template.txt).withContentType(`Content-Type`(new MediaType("text", "html")))
    case request @ POST -> "api" /: path =>
      F.flatMap(request.as[Json])(json => {
        val map = json.asObject.map(_.toMap.mapValues(_.spaces2)).get
        Ok(Await.result(MyServer.route[Api](this)(
          autowire.Core.Request(path.toList, map)
        ),10.seconds))
      })
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
    case POST -> Root / "notifications" =>
      Ok("{}")
    case request @ GET -> path =>
      static(path.toList.mkString("/"), request).getOrElseF(NotFound())
    case request @ (POST -> Root / "add") =>
      ???
  }

  //  def serveFile(path: String, request: Request): Task[Response] =
  //    StaticFile
  //      .fromFile(new File(path), Some(request))
  //      .map(Task.now)
  //      .getOrElse(NotFound())
  //
  //  def service: Service[Request, MaybeResponse] =
  //    denyNonSSLService orElse unauthedService orElse authentication.middleware(authedService)

  //  val denyNonSSLService: HttpService = HttpService {
  //    case request
  //      if request.headers
  //        .get(headers.`X-Forwarded-Proto`)
  //        .map(_.value)
  //        .getOrElse("") != "https" =>
  //      Uri.fromString(configuration.rootPath) match {
  //        case Right(uri) => TemporaryRedirect(uri)
  //        case Left(ParseFailure(sanitized, details)) => InternalServerError(s"$sanitized, $details")
  //      }
  //  }

  //  val unauthedService: HttpService = HttpService {
  //
  //    case GET -> Root / "status" =>
  //      Ok()
  //
  //    case GET -> Root / "stats" =>
  //      persistence.getStats().value.flatMap {
  //        case Right(stats) => Ok(stats)(jsonEncoderOf[Stats])
  //        case Left(err) => InternalServerError(err)
  //      }
  //
  //    case GET -> Root / "shared-wishes" / secretURL =>
  //      println(s"Fetching shared wishes for url $secretURL")
  //      val entries: EitherT[Task, String, List[WishEntry]] = for {
  //        email <- persistence.emailForSecretURL(secretURL)
  //        entries <- persistence.getEntriesFor(email)
  //      } yield entries
  //
  //      entries.value.flatMap {
  //        case Right(wishes) => Ok(wishes)(jsonEncoderOf[List[WishEntry]])
  //        case Left(error) => Ok(error)
  //      }
  //    case request@GET -> Root if request.params.contains("sharedURL") =>
  //      val secretURL = request.params.get("sharedURL").get
  //      val owner: persistence.PersistenceResponse[String] = persistence.userForSecretURL(secretURL)
  //
  //      println(s"Fetching wishes for secret $secretURL")
  //
  //      owner.value.flatMap {
  //        case Right(owner) =>
  //          Ok(html.share(secretURL, owner))
  //        case Left(err) => NotFound(err)
  //      }
  //
  //    case request@(GET -> Root) =>
  //      println(s"Got request for root")
  //      Ok(html.index())
  //
  //    case request
  //      if request.method == GET && (List(".css", ".html", ".js", ".ico", ".svg").exists(
  //        request.pathInfo.endsWith) || request.pathInfo.contains("acme-challenge")) =>
  //      println(s"Got static file request: ${request.pathInfo}")
  //      serveFile("." + request.pathInfo, request)
  //
  //    case request@(POST -> Root / "createuser") =>
  //      val createUserRequest = request.as(jsonOf[CreateUserRequest]).unsafeRun()
  //      val registrationToken = java.util.UUID.randomUUID.toString
  //      println(s"""Creating ${createUserRequest.copy(password = "")}""")
  //
  //      val token = java.util.UUID.randomUUID.toString
  //      val userCreated: Task[Either[String, String]] =
  //        persistence.createUser(createUserRequest, token).value
  //
  //      userCreated.flatMap {
  //        case Left(err) => BadRequest(err)
  //        case Right(s) =>
  //          println(s"Sending activation email to ${createUserRequest.email}")
  //          sendActivationEmail(createUserRequest.email, token)
  //          Ok(s)
  //      }
  //
  //    case GET -> Root / "finalize" / token =>
  //      persistence.finalize(token).value.flatMap {
  //        case Left(err) => BadRequest(err)
  //        case Right(s) => Ok(s)
  //      }
  //
  //  }
  //
  //  private def sendActivationEmail(email: String, token: String): String =
  //    Email(configuration.emailSettings, configuration.rootPath)
  //      .sendTo(email, token)
  //
  //  def authedService: AuthedService[User] = AuthedService {
  //
  //    case request@GET -> Root / "sharingURL" as user =>
  //      persistence.getSharingURL(user.name).value.flatMap {
  //        case Right(secret) =>
  //          val url = s"${configuration.rootPath}/?sharedURL=$secret"
  //          Ok(url)
  //        case Left(err) => NotFound(err)
  //      }
  //
  //    case request@POST -> Root / "login" as user =>
  //      println("LOGIN REQUEST" + user)
  //      Ok(user.secret)
  //        .putHeaders(Header("Set-Cookie", s"authsecret=${user.secret}; Max-Age=1337"),
  //          Header("Set-Cookie", s"authname=${user.name}; Max-Age=1337"))
  //
  //    case GET -> Root / "entries" as user =>
  //      getEntriesFor(user.name)
  //
  //    case request@POST -> Root / "set" as user =>
  //      val wishes: Task[List[Wish]] = request.req.as(jsonOf[List[Wish]])
  //
  //      wishes.flatMap(w => setWishesFor(w, user))
  //
  //    case request@POST -> Root / "grant" as user =>
  //      val wish: Task[Wish] = request.req.as(jsonOf[Wish])
  //      println(s"Granting wish ${wish} for user ${user}")
  //
  //      wish
  //        .map {
  //          case Wish(heading, desc, image) =>
  //            WishEntry(user.name, heading, desc, image.mkString, 0, -1)
  //        }
  //        .flatMap(w => persistence.grant(w).value)
  //        .flatMap {
  //          case Right(addResult) => println("WISH GRANTED"); Ok(addResult)
  //          case Left(err) => println(s"WISH NOT GRANTED $err"); InternalServerError(err)
  //        }
  //  }
  //
  //  private def getEntriesFor(username: String) = {
  //    val entries: persistence.PersistenceResponse[List[WishEntry]] =
  //      persistence.getEntriesFor(username)
  //    println(s"Getting entries for ${username}")
  //
  //    val wishes: EitherT[Task, String, List[Wish]] = entries.map {
  //      _.map {
  //        case WishEntry(_, heading, desc, image, index, id) => Wish(heading, desc, Option(image))
  //      }
  //    }
  //
  //    println(s"Found wishes $wishes")
  //
  //    wishes.value.flatMap {
  //      case Right(wishes) => Ok(wishes)(jsonEncoderOf[List[Wish]])
  //      case Left(error) => Ok(error)
  //    }
  //  }
  //
  //  private def setWishesFor(wishes: List[Wish], user: User) = {
  //
  //    val entries: List[WishEntry] = wishes.zipWithIndex.map {
  //      case (Wish(heading, desc, image), index) =>
  //        WishEntry(user.name, heading, desc, image.getOrElse(""), index, -1)
  //    }
  //
  //    println(s"Setting wishes for $user: ${wishes.mkString}")
  //
  //    persistence.set(entries, user.name).value.flatMap {
  //      case Right(addResult) => Ok(addResult)
  //      case Left(err) => InternalServerError(err)
  //    }
  //  }
  override def add(x: Int, y: Int): Future[Int] = Future.successful(x + y)
}
