package com.lambdaminute.wishr.persistence
import com.lambdaminute.wishr.model.WishEntry

case class WeakPersistence() extends Persistence {
  var db: List[WishEntry] = Nil

  override def getEntriesFor(user: String): List[WishEntry] =
    db.filter(_.user == user)

  override def set(entries: List[WishEntry]): String = {
    db = entries
    db.mkString("\n")
  }

}
