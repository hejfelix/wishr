package com.lambdaminute.wishr.config

object Module {
  type ModuleOr[+T] = Either[String, T]
}
