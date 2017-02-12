package com.lambdaminute.wishr.persistence
import com.lambdaminute.wishr.model.{Wish, WishEntry}
import fs2.Task
import io.getquill.{Literal, MirrorSqlDialect, SqlMirrorContext}

class QuillPersistence extends Persistence {

  val ctx = new SqlMirrorContext[MirrorSqlDialect, Literal]
  import ctx._


  def getEntriesFor(user: String): List[WishEntry] = ???

  def set(entries: List[WishEntry]): String = {
    val insertWishes = quote(
      liftQuery(
        entries
      ).foreach(e => query[WishEntry].insert(e)))

    val result = ctx.run(insertWishes)
    result.toString
  }



}
