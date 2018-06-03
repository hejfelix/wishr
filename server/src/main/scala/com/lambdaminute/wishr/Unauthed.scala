package com.lambdaminute.wishr

import cats.effect.IO
import com.lambdaminute.wishr.model.tags._
import com.lambdaminute.wishr.model.{UnauthedApi, UserInfo, Wish}
import com.lambdaminute.wishr.persistence.Persistence

class Unauthed(persistence: Persistence[IO, String]) extends UnauthedApi {
  override def getSharedWishes(sharedToken: String): List[Wish] =
    persistence
      .getEntriesForSecret(sharedToken.asSecretUrl)
      .map(_.map(entry => Wish(entry.heading, entry.desc, Option(entry.image), entry.id)))
      .fold(err => throw new Exception(err), identity)
      .unsafeRunSync()

  override def logIn(email: String, password: String): UserInfo =
    (for {
      token  <- persistence.logIn(email.asEmail, password.asPassword)
      dbUser <- persistence.getUserInfo(token)
    } yield {
      UserInfo(dbUser.firstName, dbUser.lastName, dbUser.email, dbUser.secretUrl, token)
    }).fold(err => throw new Exception(err), identity).unsafeRunSync()
}
