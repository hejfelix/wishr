package com.lambdaminute

import java.io.File

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

class WishRService[F[_]](applicationConf: ApplicationConf,
                         persistence: Persistence[F, String],
                         authedApi: SessionToken => AuthedApi[Id],
                         unauthed: UnauthedApi)(implicit F: Effect[F], ec: ExecutionContext)
    extends Http4sDsl[F] {

  def static(file: String, request: Request[F]): OptionT[F, Response[F]] = {
    logger.info(s"Requested file: /static/${file}")
    StaticFile.fromFile[F](new File(s"/static/$file"))
  }

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
  }

  lazy val staticFilesService = HttpService[F] {
    case request @ GET -> path =>
      println(request)
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

}
