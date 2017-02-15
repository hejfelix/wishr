package com.lambdaminute

import java.io.File

import cats.arrow.Choice
import cats.data.Kleisli
import com.lambdaminute.wishr.model.{User, Wish, WishEntry}
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

import org.http4s.server._

case class WishRService(persistence: Persistence) extends CirceInstances {

  override protected def defaultPrinter: Printer = Printer.spaces2

  def serveFile(path: String, request: Request) =
    StaticFile
      .fromFile(new File(path), Some(request))
      .map(Task.now) // This one is require to make the types match up
      .getOrElse(NotFound()) // In case the file doesn't exist

  implicit def eitherEncoder[T: Encoder, U: Encoder]: EntityEncoder[Either[T, U]] =
    jsonEncoderOf[Either[T, U]]

  //Authenticate the user
  val authUser: Kleisli[Task, Request, Either[String, User]] = Kleisli(
    req =>
      if (req.pathInfo.contains("pass")) {
        println("Got PASS request...")
        Task.now(Right(User(42, "Valid User")))
      } else
        Task.now(Left("Bad Credentials"))
  )

  val onAuthFailure: AuthedService[String] = Kleisli(req => Forbidden(req.authInfo))

  val authedService: AuthedService[User] =
    AuthedService {
      case GET -> Root / "pass" / "welcome" as user => Ok(s"Welcome, ${user}")
    }

  implicit def serviceChoice: Choice[Service] = new Choice[Service] {
    override def choice[A, B, C](f: Service[A, C], g: Service[B, C]): Service[Either[A, B], C] = Kleisli(
      (a: Either[A, B]) =>
        a match {
          case Right(r) => g(r)
          case Left(l)  => f(l)
      }
    )
    override def id[A]: Service[A, A] = Kleisli[Task, A, A](Task.now)
    override def compose[A, B, C](f: Service[B, C], g: Service[A, B]): Service[A, C] =
      Kleisli((r: A) => g(r).flatMap(x => f(x)))
  }

  def authWithFailure[Err, T](authUser: Service[Request, Either[Err, T]],
                              onFailure: Service[AuthedRequest[Err], MaybeResponse]): AuthMiddleware[T] = {
    service: Service[AuthedRequest[T], MaybeResponse] =>
      Choice[Service]
        .choice(onFailure, service)
        .local({ authed: AuthedRequest[Either[Err, T]] =>
          authed.authInfo.bimap(err => AuthedRequest(err, authed.req), suc => AuthedRequest(suc, authed.req))
        })
        .compose(AuthedRequest(authUser.run))
  }
  def middleware: AuthMiddleware[User] = authWithFailure(authUser, onAuthFailure)

  def basicAuthService: Service[Request, MaybeResponse] = middleware(authedService)

//  def service = noAuthService

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

    case request if request.method == GET && List(".css", ".html", ".js", ".ico").exists(request.pathInfo.endsWith) =>
      println(s"Got static file request: ${request.pathInfo}")
      serveFile("." + request.pathInfo, request)
  }

}
