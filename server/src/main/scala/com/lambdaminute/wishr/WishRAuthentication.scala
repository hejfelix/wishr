package com.lambdaminute.wishr

import cats.arrow.Choice
import cats.data.Kleisli
import com.lambdaminute.wishr.model.{LoginRequest, User}
import com.lambdaminute.wishr.persistence.Persistence
import fs2.Task
import fs2.interop.cats._
import io.circe.generic.auto._
import io.circe.{Encoder, Printer}
import org.http4s._
import org.http4s.circe.CirceInstances
import org.http4s.dsl._
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware

case class WishRAuthentication(persistence: Persistence[String, String]) extends CirceInstances {

  override protected def defaultPrinter: Printer = Printer.spaces2
  import cats.implicits._

  implicit def eitherEncoder[T: Encoder, U: Encoder]: EntityEncoder[Either[T, U]] =
    jsonEncoderOf[Either[T, U]]

  def trace[T](t: T): T = { println(t); t }

  //Authenticate the user
  val authUser: Kleisli[Task, Request, Either[String, User]] = Kleisli(
    request => {
      if (request.method == POST && request.pathInfo == "/login")
        handleLogin(request)
      else
        Task.now(for {
          secret   <- request.headers.get(Authorization).map(_.value).toRight("No auth header found")
          username <- persistence.getUserFor(secret).map(trace)
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
