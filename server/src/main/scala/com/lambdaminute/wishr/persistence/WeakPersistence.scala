package com.lambdaminute.wishr.persistence
import java.time.Instant
import java.time.temporal.ChronoUnit

import com.github.t3hnar.bcrypt._
import com.lambdaminute.wishr.model.{CreateUserRequest, UserSecret, WishEntry}

case class WeakPersistence() extends Persistence[String, String] {

  case class DBUser(firstName: String,
                    lastName: String,
                    email: String,
                    hashedPassword: String,
                    registrationToken: String,
                    finalized: Boolean)

  var db: List[WishEntry]           = Nil
  var userSecrets: List[UserSecret] = Nil
  var users: List[DBUser] = List(
    DBUser("Felix", "Palludan Hargreaves", "", "abekatten".bcrypt, "", true))

  def getUserFor(secret: String): Either[String, String] =
    userSecrets
      .find(s => s.secretString == secret && s.expirationDate.isAfter(Instant.now()))
      .map(_.user)
      .toRight("Token expired")

  def plusTimeout(i: Instant) = i.plus(60, ChronoUnit.SECONDS)

  private def getHashedPasswordFor(username: String) =
    users.find(_.firstName == username).map(_.hashedPassword).getOrElse("")

  override def logIn(user: String, hash: String): Either[String, String] =
    if (hash.isBcrypted(getHashedPasswordFor(user))) {

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

  override def finalize(registrationToken: String): Either[String, String] =
    if (users.exists(u => u.registrationToken == registrationToken && u.finalized == false)) {
      users = users.map { u =>
        if (u.registrationToken == registrationToken) u.copy(finalized = true) else u
      }
      Right("User successfully finalized")
    } else if (users.exists(_.registrationToken == registrationToken)) {
      Left("User already finalized")
    } else {
      Left("No user exists for given token")
    }

  override def createUser(createUserRequest: CreateUserRequest,
                          activationToken: String): Either[String, String] =
    if (users.exists(_.email == createUserRequest.email)) {
      Left("User with that e-mail already exists")
    } else {
      val cur = createUserRequest
      val user =
        DBUser(cur.firstName, cur.lastName, cur.email, cur.password.bcrypt, activationToken, false)
      users = user +: users
      Right("Successfully created user")
    }
}
