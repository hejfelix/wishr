package com.lambdaminute.wishr

import cats.effect.IO
import com.lambdaminute.wishr.model.tags._
import com.lambdaminute.wishr.model.{UnauthedApi, UserInfo, Wish}
import com.lambdaminute.wishr.persistence.Persistence

case class BadCredentialsError(message: String) extends Throwable {
  override def getMessage: String = s"Bad credentials: ${message}"
}
class Unauthed(persistence: Persistence[IO, String]) extends UnauthedApi {
  override def getSharedWishes(sharedToken: String): (List[Wish], UserInfo) =
    (for {
      entries  <- persistence.getEntriesForSecret(sharedToken.asSecretUrl)
      userInfo <- persistence.getUserInfoFromSecret(sharedToken.asSecretUrl)
    } yield
      (entries,
       UserInfo(userInfo.firstName,
                userInfo.lastName,
                userInfo.email,
                sharedToken.asSecretUrl,
                "".asSessionToken)))
      .map {
        case (entries, userInfo) =>
          (entries.map(entry => Wish(entry.heading, entry.desc, Option(entry.image), entry.id)),
           userInfo)
      }
      .fold(err => throw new Exception(err), identity)
      .unsafeRunSync()
//  persistence
//      .getEntriesForSecret(sharedToken.asSecretUrl)
//      .map(_.map(entry => Wish(entry.heading, entry.desc, Option(entry.image), entry.id)))
//      .fold(err => throw new Exception(err), identity)
//      .unsafeRunSync()

  private def cleanEmail(email: String) =
    email.trim.toLowerCase

  override def logIn(email: String, password: String): UserInfo =
    (for {
      token  <- persistence.logIn(cleanEmail(email).asEmail, password.asPassword)
      dbUser <- persistence.getUserInfo(token)
    } yield {
      UserInfo(dbUser.firstName, dbUser.lastName, dbUser.email, dbUser.secretUrl, token)
    }).fold(err => throw new BadCredentialsError(err), identity).unsafeRunSync()
}
