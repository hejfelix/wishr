package com.lambdaminute.wishr

import cats.Id
import cats.effect.IO
import com.lambdaminute.wishr.model.tags._
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.persistence.Persistence

class Authed(token: SessionToken, persistence: Persistence[IO, String]) extends AuthedApi[Id] {

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
