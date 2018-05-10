package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.{Email, SecretUrl}

case class UserInfo(firstName: String, lastName: String, email: Email, secretUrl: SecretUrl)

