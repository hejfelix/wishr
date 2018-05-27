package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.{Email, SecretUrl}
import com.lambdaminute.wishr.model.tags._
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}

object UserInfo {
  implicit val decoder:Decoder[UserInfo] = (c: HCursor) =>
    for {
      fname <- c.get[String]("firstName")
      lname <- c.get[String]("lastName")
      email <- c.get[String]("email")
      secretUrl <- c.get[String]("secretUrl")
    } yield UserInfo(fname,lname,email.asEmail,secretUrl.asSecretUrl)

}
case class UserInfo(firstName: String, lastName: String, email: Email, secretUrl: SecretUrl)

