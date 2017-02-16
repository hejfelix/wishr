package com.lambdaminute.wishr

import cats.arrow.Choice
import cats.data.Kleisli
import com.lambdaminute.wishr.model.{LoginRequest, User}
import com.lambdaminute.wishr.persistence.Persistence
import fs2.Task
import io.circe.{Encoder, Printer}
import org.http4s._
import org.http4s.circe.CirceInstances
import org.http4s.dsl._
import org.http4s.server.AuthMiddleware
import io.circe.generic.auto._
import fs2.interop.cats._
import org.http4s.headers
import org.http4s.headers.Authorization
import org.http4s.util.CaseInsensitiveString

case class WishRAuthentication(persistence: Persistence[String, String])
    extends CirceInstances {

  override protected def defaultPrinter: Printer = Printer.spaces2
  import cats._
  import cats.implicits._

  implicit def eitherEncoder[T: Encoder, U: Encoder]: EntityEncoder[Either[T, U]] =
    jsonEncoderOf[Either[T, U]]

  //Authenticate the user
  val authUser: Kleisli[Task, Request, Either[String, User]] = Kleisli(
    request => {
      if (request.method == POST && request.pathInfo == "/login")
        handleLogin(request)
      else
        Task.now(for {
          secret   <- request.headers.get(Authorization).map(_.value).toRight("No auth header found")
          username <- persistence.getUserFor(secret)
        } yield User(username, secret))
    }
  )

  private def handleLogin(request: Request): Task[Either[String, User]] =
    request
      .as(jsonOf[LoginRequest])
      .map {
        case LoginRequest(user, password) =>
          persistence.logIn(user, password).map(secret => User(user, secret))
      }

  val onAuthFailure: AuthedService[String] = Kleisli(req => Forbidden(req.authInfo))

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
}
