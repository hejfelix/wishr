package com.lambdaminute.wishr.model

sealed trait ServiceError {
  def msg: String
}

case class DatabaseError(t: Throwable) extends ServiceError {
  def msg = t.getMessage
}

case class MissingValueError(msg: String) extends ServiceError
