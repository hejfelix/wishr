package com.lambdaminute.wishr.model

trait AuthedApi[F[_]] {
  def me(): F[UserInfo]
  def getWishes(): F[WishList]
  def updateWish(wish: Wish): F[Unit]
  def deleteWish(wishId: Int): F[Unit]
  def newWish(): F[Wish]
}
