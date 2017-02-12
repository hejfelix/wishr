package com.lambdaminute.wishr.persistence

import com.lambdaminute.wishr.model.WishEntry
import fs2.Task

trait Persistence {

  def getEntriesFor(user: String): List[WishEntry]
  def set(entries: List[WishEntry]): String

}
