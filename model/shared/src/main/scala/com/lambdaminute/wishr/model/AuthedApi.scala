package com.lambdaminute.wishr.model

import scala.concurrent.Future

trait AuthedApi {
  def add(x: Int, y: Int): Future[Int]
  def getWishes(): Future[WishList]
  def updateWish(wish: Wish): Future[Unit]
  def createWish(wish: Wish): Future[Unit]
}

