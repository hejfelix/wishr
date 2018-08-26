package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags._
import io.circe.{Decoder, HCursor}

object LoginRequest {
  implicit val decoder: Decoder[LoginRequest] = (c: HCursor) => for {
    email <- c.get[String]("email")
    pass <- c.get[String]("password")
  } yield LoginRequest(email.asEmail, pass.asPassword)
}
case class LoginRequest(email: Email, password: Password)