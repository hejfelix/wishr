package com.lambdaminute.wishr.auth

import com.github.t3hnar.bcrypt._
case class BcryptEncryption() extends Encryption {
  override def newToken = java.util.UUID.randomUUID().toString

  override def hash(s: String) = s.bcrypt

  override def isHashedAs(str: String, hash: String) = str isBcrypted hash
}
