package com.lambdaminute.wishr

import cats.Id
import cats.effect.IO
import com.lambdaminute.wishr.model.tags.SessionToken
import com.lambdaminute.wishr.model.{AuthedApi, UserInfo, Wish, WishList}
import com.lambdaminute.wishr.persistence.Persistence

class Authed(token: SessionToken, persistence: Persistence[IO, String]) extends AuthedApi[Id] {

  override def me(): UserInfo =
    persistence
      .getUserInfo(token)
      .leftSemiflatMap(err => IO.raiseError[UserInfo](new Exception(err)))
      .fold(x => x,
            dbuser => UserInfo(dbuser.firstName, dbuser.lastName, dbuser.email, dbuser.secretUrl))
      .unsafeRunSync()

  override def add(x: Int, y: Int): Int = ???

  override def getWishes(): WishList = ???

  override def updateWish(wish: Wish): Unit = ???

  override def createWish(wish: Wish): Unit = ???
}
