package com.lambdaminute.wishr

import com.lambdaminute.wishr.persistence.Persistence
import io.circe.Printer

class WishRAuthentication[F[_]](persistence: Persistence[F,String])  {


//  implicit def eitherEncoder[T: Encoder, U: Encoder]: EntityEncoder[Either[T, U]] =
//    jsonEncoderOf[Either[T, U]]

  def trace[T](t: T): T = {
    println(t); t
  }

  //Authenticate the user
//  val authUser: Kleisli[F, Request, Either[String, User]] = Kleisli(
//    request => {
//      trace(request)
//      if (request.method == POST && request.pathInfo == "/login")
//        handleLogin(request).value
//      else {
//        EitherT[F, String, String](
//          Task.now(
//            request.headers.get(Authorization).map(_.value).toRight("No auth header found")))
//          .flatMap(userFor)
//          .value
//      }
//    }
//  )

//  private def userFor(secret: String): EitherT[Task, String, User] =
//    persistence.getUserFor(secret).map { user =>
//      User(user, secret)
//    }

//  private def handleLogin(request: Request): EitherT[Task, String, User] =
//    EitherT[Task, String, LoginRequest](request.as(jsonOf[LoginRequest]).map(Right.apply))
//      .flatMap { lr =>
//        persistence
//          .logIn(lr.user, lr.password)
//          .map(trace)
//          .map(secret => User(lr.user, secret))
//      }

//  val onAuthFailure: AuthedService[String] = Kleisli(req => {
//    Forbidden(req.authInfo).removeCookie("authcookie")
//  })

//  def authWithFailure[Err, T](
//                               authUser: Service[Request, Either[Err, T]],
//                               onFailure: Service[AuthedRequest[Err], MaybeResponse]): AuthMiddleware[T] = {
//    service: Service[AuthedRequest[T], MaybeResponse] =>
//      Choice[Service]
//        .choice(onFailure, service)
//        .local({ authed: AuthedRequest[Either[Err, T]] =>
//          authed.authInfo.bimap(err => AuthedRequest(err, authed.req),
//            suc => AuthedRequest(suc, authed.req))
//        })
//        .compose(AuthedRequest(authUser.run))
//  }
//
//  def middleware: AuthMiddleware[User] = authWithFailure(authUser, onAuthFailure)
}
