package com.lambdaminute.wishr.persistence

import com.lambdaminute.wishr.model.WishEntry
import fs2.Task

trait Persistence {

  def logIn(user: String, hash: String): Option[String]

  def getSecretFor(user: String): Option[String]

  def getEntriesFor(user: String): List[WishEntry]

  def set(entries: List[WishEntry]): String

}
