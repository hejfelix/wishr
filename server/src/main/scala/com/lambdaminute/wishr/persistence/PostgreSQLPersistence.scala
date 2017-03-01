package com.lambdaminute.wishr.persistence

import cats.free.Free
import com.lambdaminute.wishr.config.DBConfig
import doobie.free.connection.ConnectionOp
import doobie.imports.{DriverManagerTransactor, IOLite, Update0}
import doobie.imports._
import org.h2.tools.Server
import cats._
import cats.data._
import cats.implicits._
import fs2.interop.cats._
import com.github.t3hnar.bcrypt._
import com.lambdaminute.wishr.model.{CreateUserRequest, WishEntry}

case class PostgreSQLPersistence(dbconf: DBConfig) extends Persistence[String, String] {

  val xa = DriverManagerTransactor[IOLite](
    "org.postgresql.Driver",
    dbconf.url+"?sslmode=require",
    dbconf.user,
    dbconf.password
  )

  val createUsersTable: Update0 =
    sql"""
    CREATE TABLE IF NOT EXISTS users (
      firstName          VARCHAR,
      lastName           VARCHAR,
      email              VARCHAR UNIQUE,
      hashedPassword     VARCHAR,
      registrationToken  VARCHAR,
      finalized          BOOLEAN
      )
  """.update

  val createSecretsTable: Update0 = sql"""
    CREATE TABLE IF NOT EXISTS secrets (
       email            VARCHAR UNIQUE,
       secret           VARCHAR,
       expirationDate   TIMESTAMP
      )
   """.update

  val createWishesTable: Update0 = sql"""
    CREATE TABLE IF NOT EXISTS wishes (
       email                    VARCHAR,
       heading                  VARCHAR,
       description              VARCHAR,
       imageURL                 VARCHAR,
       id                       SERIAL
      )
   """.update

  val createUsersResult: Int = createUsersTable.run.transact(xa).unsafePerformIO
  println(s"Create users: $createUsersResult")
  val createSecretsResult: Int = createSecretsTable.run.transact(xa).unsafePerformIO
  println(s"Create secrets: $createSecretsResult")
  val createWishesResult: Int = createWishesTable.run.transact(xa).unsafePerformIO
  println(s"Create wishes: $createWishesResult")

  case class UserPass(firstName: String, hashedPassword: String)

  override def logIn(email: String, hash: String): Either[String, String] = {
    val selectEmail =
      sql"""SELECT email, hashedPassword FROM users WHERE email=$email""".query[UserPass]

    val userPass: List[UserPass] = selectEmail.list.transact(xa).unsafePerformIO
    userPass match {
      case UserPass(_, storedHash) :: Nil if hash.isBcrypted(storedHash) =>
        getOrCreateSecretFor(email)
      case _ => Left("Bad user credentials")
    }
  }

  private def newSecret = java.util.UUID.randomUUID.toString
  private def getOrCreateSecretFor(email: String): Either[String, String] = {

    val insertOrUpdate =
      sql"""
    INSERT INTO secrets (email, secret, expirationDate)
      VALUES ($email, $newSecret, CURRENT_TIMESTAMP + INTERVAL '60 seconds')
    ON CONFLICT (email) DO UPDATE
      SET expirationDate = CURRENT_TIMESTAMP + INTERVAL '60 seconds'
         """.update.run

    val token: Free[ConnectionOp, (Int, String)] = for {
      updateCount <- insertOrUpdate
      token       <- sql"SELECT secret FROM secrets WHERE email=$email".query[String].unique
    } yield (updateCount, token)

    token.transact(xa).unsafePerformIO match {
      case (1, token) => Right(token)
      case _          => Left(s"Unable to update and retrieve token for $email")
    }
  }

  override def getSecretFor(email: String): Either[String, String] =
    sql"SELECT secret FROM secrets WHERE email=$email and now() < expirationDate"
      .query[String]
      .option
      .map(_.toRight("Token expired"))
      .transact(xa)
      .unsafePerformIO

  override def getUserFor(secret: String): Either[String, String] = {
    val userFor =
      sql"SELECT email FROM secrets WHERE secret=$secret AND now() < expirationDate"
        .query[String]
        .option
        .map(_.toRight("Token expired"))
    userFor.transact(xa).unsafePerformIO
  }

  override def getEntriesFor(email: String): Either[String, List[WishEntry]] =
    sql"SELECT email, heading, description, imageURL, id FROM wishes WHERE email=$email"
      .query[WishEntry]
      .list
      .attemptSql
      .map(_.left.map(_.getMessage))
      .transact(xa)
      .unsafePerformIO

  private def entryToTuple(w: WishEntry): (String, String, String, String) =
    (w.email, w.heading, w.desc, w.image)

  private def setEntriesFor(email: String, entries: List[WishEntry]) =
    for {
      deleteCount <- sql"DELETE FROM wishes WHERE email=$email".update.run
      insertCount <- Update[(String, String, String, String)](
        "INSERT INTO wishes (email, heading, description, imageURL) VALUES (?, ?, ?, ?)")
        .updateMany(entries.map(entryToTuple))
    } yield
      if (insertCount == entries.length) Right(s"Successfully updated $insertCount wishes")
      else Left("Failed updating wishes")

  override def set(entries: List[WishEntry]): Either[String, String] =
    entries match {
      case Nil     => Right("No wishes to add")
      case x :: xs => setEntriesFor(x.email, entries).transact(xa).unsafePerformIO
    }

  override def finalize(registrationToken: String): Either[String, String] =
    sql"UPDATE users SET finalized=TRUE WHERE registrationToken=$registrationToken".update.run
      .map(n =>
        if (n == 1) Right("Successfully activated user")
        else Left("No user for that token was found"))
      .transact(xa)
      .unsafePerformIO

  override def createUser(createUserRequest: CreateUserRequest,
                          activationToken: String): Either[String, String] = {
    val up = Update[DBUser](
      "insert into users (firstName, lastName, email, hashedPassword, registrationToken, " +
        "finalized) values (?, ?, ?, ?, ?, ?)")
    val c                              = createUserRequest
    val dbu                            = DBUser(c.firstName, c.lastName, c.email, c.password.bcrypt, activationToken, false)
    val insertQuery: ConnectionIO[Int] = up.run(dbu)
    val result: Int                    = insertQuery.transact(xa).unsafePerformIO
    result match {
      case 1 => Right("Successfully created user")
      case _ => Left("Failed creating user in db")
    }
  }
}
