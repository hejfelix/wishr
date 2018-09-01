package com.lambdaminute.wishr.model

trait UnauthedApi {
  def logIn(email: String, password: String): UserInfo
  def getSharedWishes(sharedToken: String): (List[Wish], UserInfo)
}
