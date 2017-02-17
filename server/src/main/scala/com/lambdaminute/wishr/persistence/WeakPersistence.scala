package com.lambdaminute.wishr.persistence
import com.lambdaminute.wishr.model.{UserSecret, WishEntry}
import com.github.t3hnar.bcrypt._
import java.time.Instant
import java.time.temporal.{ChronoUnit, Temporal, TemporalAmount}

case class WeakPersistence() extends Persistence[String, String] {

  var db: List[WishEntry]                = Nil
  var userSecrets: List[UserSecret]      = Nil
  var hashPasswords: Map[String, String] = Map("admin" -> "password".bcrypt)

  def getUserFor(secret: String): Either[String, String] =
    userSecrets
      .find(s => s.secretString == secret && s.expirationDate.isAfter(Instant.now()))
      .map(_.user)
      .toRight("Token expired")

  def plusTimeout(i: Instant) = i.plus(20, ChronoUnit.SECONDS)

  override def logIn(user: String, hash: String): Either[String, String] =
    if (hash.isBcrypted(hashPasswords.getOrElse(user, ""))) {

      val newSecret = java.util.UUID.randomUUID.toString

      if (userSecrets.exists(_.user == user)) {
        userSecrets = userSecrets.map(
          s =>
            if (s.user == user)
              s.copy(expirationDate = plusTimeout(Instant.now), secretString = newSecret)
            else
            s
        )
      } else {
        userSecrets = UserSecret(user, newSecret, plusTimeout(Instant.now)) +: userSecrets
      }
      Right(newSecret)
    } else {
      Left("Invalid credentials")
    }

  override def getSecretFor(user: String): Either[String, String] = {
    println(userSecrets)
    val now = Instant.now()
    userSecrets.find(_.user == user) match {
      case Some(secret) if secret.expirationDate.isAfter(now) =>
        userSecrets = userSecrets.map(
          s =>
            if (s.user == user)
              s.copy(expirationDate = now)
            else
            s)
        Right(secret.secretString)
      case _ => Left("Token expired")
    }
  }

  override def getEntriesFor(user: String): Either[String, List[WishEntry]] =
    Right(db.filter(_.user == user))

  override def set(entries: List[WishEntry]): Either[String, String] = {
    db = entries
    Right(db.mkString("\n"))
  }

}
