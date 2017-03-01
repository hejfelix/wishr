package com.lambdaminute.wishr.persistence

import cats.free.Free
import com.github.t3hnar.bcrypt._
import com.lambdaminute.wishr.model.{CreateUserRequest, WishEntry}
import doobie.free.connection.{ConnectionIO, ConnectionOp}
import doobie.imports._
import org.h2.tools.Server
import cats._, cats.data._, cats.implicits._
import fs2.interop.cats._
case class InMemoryH2(dbConfigKey: String, port: Int) extends Persistence[String, String] {

  val h2Server = Server.createTcpServer("-tcpAllowOthers").start()

  val xa = DriverManagerTransactor[IOLite](
    "org.h2.Driver",
    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "sa",
    ""
  )

  val createUsersTable: Update0 =
    sql"""
    CREATE TABLE users (
      firstName          VARCHAR,
      lastName           VARCHAR,
      email              VARCHAR UNIQUE,
      hashedPassword     VARCHAR,
      registrationToken  VARCHAR,
      finalized          BOOLEAN
      )
  """.update

  val createSecretsTable: Update0 = sql"""
    CREATE TABLE secrets (
       user             VARCHAR UNIQUE,
       secret           VARCHAR,
       expirationDate   TIMESTAMP
      )
   """.update

  val createWishesTable: Update0 = sql"""
    CREATE TABLE wishes (
       user                     VARCHAR,
       heading                  VARCHAR,
       description              VARCHAR,
       imageURL                 VARCHAR,
       id                       INT NOT NULL AUTO_INCREMENT
      )
   """.update

  val createUsersResult: Int = createUsersTable.run.transact(xa).unsafePerformIO
  println(s"Create users: $createUsersResult")
  val createSecretsResult: Int = createSecretsTable.run.transact(xa).unsafePerformIO
  println(s"Create secrets: $createSecretsResult")
  val createWishesResult: Int = createWishesTable.run.transact(xa).unsafePerformIO
  println(s"Create wishes: $createWishesResult")

  case class UserPass(firstName: String, hashedPassword: String)

  override def logIn(user: String, hash: String): Either[String, String] = {
    val selectUser =
      sql"""SELECT firstName, hashedPassword FROM users WHERE firstName=$user""".query[UserPass]

    val userPass: List[UserPass] = selectUser.list.transact(xa).unsafePerformIO
    userPass match {
      case UserPass(_, storedHash) :: Nil if hash.isBcrypted(storedHash) =>
        getOrCreateSecretFor(user)
      case _ => Left("Bad user credentials")
    }
  }

  private def newSecret = java.util.UUID.randomUUID.toString
  private def getOrCreateSecretFor(user: String): Either[String, String] = {

    val insertOrUpdate =
      sql"""
    INSERT INTO secrets (user, secret, expirationDate) VALUES ($user, $newSecret, DATEADD('SECOND',60,
    CURRENT_TIMESTAMP()))
      ON DUPLICATE KEY UPDATE expirationDate=DATEADD('SECOND',60, CURRENT_TIMESTAMP())
         """.update.run

    val token: Free[ConnectionOp, (Int, String)] = for {
      updateCount <- insertOrUpdate
      token       <- sql"SELECT secret FROM secrets WHERE user=$user".query[String].unique
    } yield (updateCount, token)

    token.transact(xa).unsafePerformIO match {
      case (1, token) => Right(token)
      case _          => Left(s"Unable to update and retrieve token for $user")
    }
  }

  override def getSecretFor(user: String): Either[String, String] =
    sql"SELECT secret FROM secrets WHERE user=$user and now() < expirationDate"
      .query[String]
      .option
      .map(_.toRight("Token expired"))
      .transact(xa)
      .unsafePerformIO

  override def getUserFor(secret: String): Either[String, String] = {
    val userFor =
      sql"SELECT user FROM secrets WHERE secret=$secret AND now() < expirationDate"
        .query[String]
        .option
        .map(_.toRight("Token expired"))
    userFor.transact(xa).unsafePerformIO
  }

  override def getEntriesFor(user: String): Either[String, List[WishEntry]] =
    sql"SELECT user, heading, description, imageURL, id FROM wishes WHERE user=$user"
      .query[WishEntry]
      .list
      .attemptSql
      .map(_.left.map(_.getMessage))
      .transact(xa)
      .unsafePerformIO

  private def entryToTuple(w: WishEntry): (String, String, String, String) =
    (w.email, w.heading, w.desc, w.image)

  private def setEntriesFor(user: String, entries: List[WishEntry]) =
    for {
      deleteCount <- sql"DELETE FROM wishes WHERE user=$user".update.run
      insertCount <- Update[(String, String, String, String)](
        "INSERT INTO wishes (user, heading, description, imageURL) VALUES (?, ?, ?, ?)")
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
