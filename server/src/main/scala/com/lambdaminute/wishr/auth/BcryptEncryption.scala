package com.lambdaminute.wishr.auth

import com.github.t3hnar.bcrypt._
case class BcryptEncryption() extends Encryption {
  override def newToken: String                               = java.util.UUID.randomUUID().toString
  override def hash(s: String): String                        = s.bcrypt
  override def isHashedAs(str: String, hash: String): Boolean = str isBcrypted hash
}
