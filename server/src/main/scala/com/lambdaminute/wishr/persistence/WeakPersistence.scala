package com.lambdaminute.wishr.persistence
import com.lambdaminute.wishr.model.{UserSecret, WishEntry}
import com.github.t3hnar.bcrypt._

import java.time.Instant

case class WeakPersistence() extends Persistence {

  var db: List[WishEntry]                = Nil
  var userSecrets: List[UserSecret]      = Nil
  var hashPasswords: Map[String, String] = Map("admin" -> "password".bcrypt)

  override def logIn(user: String, hash: String): Option[String] =
    if (hash.isBcrypted(hashPasswords.getOrElse(user, ""))) {

      val newSecret = java.util.UUID.randomUUID.toString

      if (userSecrets.exists(_.user == user)) {
        userSecrets = userSecrets.map(
          s =>
            if (s.user == user)
              s.copy(expirationDate = Instant.now, secretString = newSecret)
            else
            s
        )
      } else {
        userSecrets = UserSecret(user, newSecret, Instant.now) +: userSecrets
      }

      Option(newSecret)
    } else {
      None
    }

  override def getSecretFor(user: String): Option[String] = {
    val now = Instant.now()
    userSecrets.find(_.user == user) match {
      case Some(secret) if secret.expirationDate.isAfter(now) =>
        userSecrets = userSecrets.map(
          s =>
            if (s.user == user)
              s.copy(expirationDate = now)
            else
            s)
        Option(secret.secretString)
      case _ => None
    }
  }

  override def getEntriesFor(user: String): List[WishEntry] =
    db.filter(_.user == user)

  override def set(entries: List[WishEntry]): String = {
    db = entries
    db.mkString("\n")
  }

}
