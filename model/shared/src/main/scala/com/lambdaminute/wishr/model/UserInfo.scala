package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.{Email, SecretUrl, _}
import io.circe.{Decoder, HCursor}

object UserInfo {
  implicit val decoder: Decoder[UserInfo] = (c: HCursor) =>
    for {
      fname     <- c.get[String]("firstName")
      lname     <- c.get[String]("lastName")
      email     <- c.get[String]("email")
      secretUrl <- c.get[String]("secretUrl")
      token     <- c.get[String]("sessionToken")
    } yield UserInfo(fname, lname, email.asEmail, secretUrl.asSecretUrl, token.asSessionToken)

}
case class UserInfo(firstName: String,
                    lastName: String,
                    email: Email,
                    secretUrl: SecretUrl,
                    sessionToken: SessionToken)
