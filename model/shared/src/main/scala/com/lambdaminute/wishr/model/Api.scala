package com.lambdaminute.wishr.model

import scala.concurrent.Future

trait Api {
  def add(x: Int, y: Int): Future[Int]
}
