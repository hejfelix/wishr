package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.{Email, Password}

import scala.concurrent.Future

trait UnauthedApi {
  def logIn(email: String, password: String): UserInfo
  def getSharedWishes(sharedToken: String): (List[Wish], UserInfo)
}
