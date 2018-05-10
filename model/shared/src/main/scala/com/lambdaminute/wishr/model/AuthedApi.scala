package com.lambdaminute.wishr.model

trait AuthedApi[F[_]] {
  def me(): F[UserInfo]
  def add(x: Int, y: Int): F[Int]
  def getWishes(): F[WishList]
  def updateWish(wish: Wish): F[Unit]
  def createWish(wish: Wish): F[Unit]
}
