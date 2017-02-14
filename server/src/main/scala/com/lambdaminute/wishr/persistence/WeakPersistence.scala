package com.lambdaminute.wishr.persistence
import com.lambdaminute.wishr.model.WishEntry

class DictionaryPersistence extends Persistence {
  var db: List[WishEntry] = Nil

  override def getEntriesFor(user: String): List[WishEntry] =
    db.filter(_.user == user).toList

  override def set(entries: List[WishEntry]): String = {
    db = entries
    db.mkString("\n")
  }

}
