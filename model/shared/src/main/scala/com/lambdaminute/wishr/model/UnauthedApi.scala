package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.{Email, Password}

import scala.concurrent.Future

trait UnauthedApi[F[_]] {
  def logIn(email: Email, password: Password): F[UserInfo]
}
