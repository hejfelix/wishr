package com.lambdaminute

import cats.Id
import cats.data.{EitherT, Kleisli, OptionT}
import cats.effect.Effect
import cats.implicits._
import com.lambdaminute.wishr.WishRApp
import com.lambdaminute.wishr.config.ApplicationConf
import com.lambdaminute.wishr.model.tags._
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.persistence.Persistence
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.server._
import org.http4s.circe._
import org.http4s.{AuthedService, Header, HttpService, MediaType, Request, Response, StaticFile}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object MyServer extends autowire.Server[String, Decoder, Encoder] {

  def read[Result](p: String)(implicit evidence$1: Decoder[Result]): Result =
    decode[Result](p).right.get

  def write[Result](r: Result)(implicit evidence$2: Encoder[Result]): String =
    r.asJson.spaces2

}

class Template {
  import scalatags.Text.all._
  import scalatags.Text.tags2.title

  def txt(jsPath: String) =
    "<!DOCTYPE html>" +
      html(
        head(
          title("Example Scala.js application"),
          meta(httpEquiv := "Content-Type", content := "text/html; charset=UTF-8"),
          script(`type` := "text/javascript", src := jsPath)
        ),
        body(margin := 0)(
          div(id := "root"),
          script("Main.main")
        )
      )
}

class WishRService[F[_]](applicationConf: ApplicationConf,
                         persistence: Persistence[F, String],
                         authedApi: SessionToken => AuthedApi[Id],
                         unauthed: UnauthedApi)(implicit F: Effect[F], ec: ExecutionContext)
    extends Http4sDsl[F] {

  private val template = new Template()

  def static(file: String, request: Request[F]): OptionT[F, Response[F]] =
    StaticFile.fromResource(s"/$file", Some(request))

  import org.log4s._
  private val logger = getLogger("BLOOP")

  private val loggingService: Kleisli[OptionT[F, ?], Request[F], Request[F]] = Kleisli { x =>
    for {
      body <- OptionT(x.bodyAsText.compile.foldSemigroup)
      _ = logger.debug(s"${x.method}: ${x.pathInfo}, ${body}")
      result <- OptionT.pure[F](x)
    } yield result
  }

  implicit val decoder = jsonOf[F, LoginRequest](F, LoginRequest.decoder)
  implicit val encoder = jsonOf[F, UserInfo]

  val unauthedService: HttpService[F] = loggingService andThen HttpService[F] {
//    case GET -> Root =>
//      Ok(template.txt("../client/target/scala-2.12/scalajs-bundler/main/client-fastopt.js")) //.withContentType(`Content-Type`(new MediaType("text", "html")))
    case request @ POST -> path =>
      F.flatMap(request.as[Json]) { json =>
        println(s"Unauthed request to path ${path.toList}")
        val map = json.asObject.map(_.toMap.mapValues(_.spaces2)).get
        println(json)
        println(map)
        val routedResult: Future[String] = MyServer.route[UnauthedApi](unauthed)(
          autowire.Core.Request(path.toList, map)
        )
        Ok(Await.result(routedResult, 10.seconds))
      }
//
//      (for {
//        body <- EitherT.right(request.bodyAsText.compile.foldSemigroup)
//        _ = println(s"login: ${body}")
//        loginRequest <- EitherT.right[String](request.as[LoginRequest])
//        _ = logger.debug(s"Login request: ${loginRequest}")
//        token  <- persistence.logIn(loginRequest.email, loginRequest.password)
//        dbUser <- persistence.getUserInfo(token)
//      } yield {
//        (dbUser, token)
//      }).semiflatMap {
//          case (dbUser, token) =>
//            val spaces = dbUser.asJson.spaces2
//            println(spaces)
//            Ok(UserInfo(dbUser.firstName, dbUser.lastName, dbUser.email, dbUser.secretUrl, token).asJson)
//              .map(_.addCookie(sessionTokenHeaderKey, token))
//        }
//        .leftSemiflatMap(InternalServerError(_))
//        .fold(identity, identity)
  } <+> staticFilesService

  lazy val staticFilesService = HttpService[F] {
    case request @ GET -> path =>
      static(path.toList.mkString("/"), request).getOrElseF(NotFound())
  }

  lazy val retrieveUser: Kleisli[F, SessionToken, Either[String, User]] = Kleisli {
    (token: SessionToken) =>
      persistence
        .emailForSessionToken(token)
        .map { email =>
          println(email)
          User(email, token)
        }
        .value
  }

  lazy val onFailure: AuthedService[String, F] = Kleisli(
    req => {
      println(s"FAILED AUTH ${req} ${req.authInfo}")
      OptionT.liftF(Forbidden(req.authInfo))
    }
  )

  lazy val sessionTokenHeaderKey = "sessiontoken"

  lazy val authUser: Kleisli[F, Request[F], Either[String, User]] = Kleisli { request =>
    val headersList = request.headers.toList
    println(headersList)
    println(headersList.find(_.name.toString == sessionTokenHeaderKey).map(_.value))
    val token: Option[Header] = headersList.find(_.name.toString() == sessionTokenHeaderKey)
    token
      .map(_.value.asSessionToken)
      .toRight("No session token found")
      .fold(err => F.pure(Left(err)), retrieveUser.run)
  }

  lazy val authMiddleWare: AuthMiddleware[F, User] = AuthMiddleware(authUser, onFailure)

  lazy val authedService: AuthedService[User, F] = AuthedService {
    case request @ (POST -> path) as user =>
      F.flatMap(request.req.as[Json])(json => {
        println(s"Authed request: $request")
        val map = json.asObject.map(_.toMap.mapValues(_.spaces2)).get
        println(map)
        val routedResult: Future[String] = MyServer.route[AuthedApi[Id]](authedApi(user.secret))(
          autowire.Core.Request(path.toList, map)
        )
        Ok(Await.result(routedResult, 10.seconds))
      })
  }

  lazy val wrappedAuthedService: HttpService[F] = loggingService andThen authMiddleWare(
    authedService)

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

}
