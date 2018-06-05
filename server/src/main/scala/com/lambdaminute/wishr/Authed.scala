package com.lambdaminute.wishr

import cats.Id
import cats.effect.IO
import com.lambdaminute.wishr.model.tags._
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.persistence.Persistence

class Authed(token: SessionToken, persistence: Persistence[IO, String]) extends AuthedApi[Id] {

  def md5HashString(s: String): String = {
    import java.security.MessageDigest
    import java.math.BigInteger
    val md           = MessageDigest.getInstance("MD5")
    val digest       = md.digest(s.getBytes)
    val bigInt       = new BigInteger(1, digest)
    val hashedString = bigInt.toString(16)
    Stream.continually("0").take(32 - hashedString.length).mkString + hashedString
  }

  override def gravatarUrl(): Id[String] =
    persistence
      .emailForSessionToken(token)
      .map(email => s"https://s.gravatar.com/avatar/${md5HashString(email)}?d=identicon")
      .fold(err => throw new Exception(err), identity)
      .unsafeRunSync()

  override def me(): UserInfo =
    persistence
      .getUserInfo(token)
      .leftSemiflatMap(err => IO.raiseError[UserInfo](new Exception(err)))
      .fold(x => x,
            dbuser =>
              UserInfo(dbuser.firstName, dbuser.lastName, dbuser.email, dbuser.secretUrl, token))
      .unsafeRunSync()

  override def newWish(): Id[Wish] =
    (for {
      userInfo <- persistence.getUserInfo(token)
      newWishId <- persistence.createWish(email = userInfo.email,
                                          heading = "",
                                          descr = "",
                                          imageUrl = None)
    } yield Wish("", "", None, newWishId))
      .fold(err => throw new Exception(err), identity)
      .unsafeRunSync()

  override def getWishes(): WishList =
    (for {
      userInfo <- persistence.getUserInfo(token)
      wishes   <- persistence.getEntriesFor(userInfo.email)
    } yield (wishes, userInfo))
      .leftSemiflatMap(err => IO.raiseError[WishList](new Exception(err)))
      .fold(x => x, {
        case (wishes, userInfo) =>
          WishList(userInfo.email,
                   wishes.map(w => Wish(w.heading, w.desc, Option(w.image), w.id.asWishId)))
      })
      .unsafeRunSync()

  override def updateWish(wish: Wish): Unit =
    (for {
      userInfo <- persistence.getUserInfo(token)
      _        <- persistence.updateWish(wish)
    } yield ()).fold(err => throw new Exception(err), identity _).unsafeRunSync()

  override def deleteWish(id: Int): Id[Unit] =
    persistence
      .deleteWish(id.asWishId)
      .fold(err => throw new Exception(err), identity)
      .unsafeRunSync()
}
