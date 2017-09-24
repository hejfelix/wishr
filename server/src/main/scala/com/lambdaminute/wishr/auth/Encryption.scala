package com.lambdaminute.wishr.auth

trait Encryption {
  def newToken: String
  def hash(s: String): String
  def isHashedAs(str: String, hash: String): Boolean
}
