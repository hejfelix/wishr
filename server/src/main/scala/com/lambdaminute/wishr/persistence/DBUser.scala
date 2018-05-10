package com.lambdaminute.wishr.persistence

import com.lambdaminute.wishr.model.tags.{Email, SecretUrl}

case class DBUser(firstName: String, lastName: String, email: Email, secretUrl: SecretUrl)

