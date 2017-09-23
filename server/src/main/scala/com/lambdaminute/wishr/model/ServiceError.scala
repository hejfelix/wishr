package com.lambdaminute.wishr.model

sealed trait ServiceError

case class DatabaseError(t: Throwable) extends ServiceError
