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
import fs2.Task
import cats.data.EitherT

case class PostgreSQLPersistence(dbconf: DBConfig) extends Persistence[String, String] {

  val xa = DriverManagerTransactor[Task](
                                          "org.postgresql.Driver",
                                          dbconf.url + "?sslmode=require",
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
      secretURL          VARCHAR,
      registrationToken  VARCHAR,
      finalized          BOOLEAN
      )
  """.update

  val createSecretsTable: Update0 =
    sql"""
    CREATE TABLE IF NOT EXISTS secrets (
       email            VARCHAR UNIQUE,
       secret           VARCHAR,
       expirationDate   TIMESTAMP
      )
   """.update

  val createWishesTable: Update0 =
    sql"""
    CREATE TABLE IF NOT EXISTS wishes (
       email                    VARCHAR,
       heading                  VARCHAR,
       description              VARCHAR,
       imageURL                 VARCHAR,
       index                    INTEGER,
       id                       SERIAL
      )
   """.update

  val createGrantedTable: Update0 =
    sql"""
    CREATE TABLE IF NOT EXISTS granted (
       email                    VARCHAR,
       heading                  VARCHAR,
       description              VARCHAR,
       imageURL                 VARCHAR,
       index                    INTEGER,
       id                       SERIAL
      )
   """.update

  val createUsersResult: Int = createUsersTable.run.transact(xa).unsafeRun()
  println(s"Create users: $createUsersResult")
  val createSecretsResult: Int = createSecretsTable.run.transact(xa).unsafeRun()
  println(s"Create secrets: $createSecretsResult")
  val createWishesResult: Int = createWishesTable.run.transact(xa).unsafeRun()
  println(s"Create wishes: $createWishesResult")
  val createGrantedTableResult: Int = createGrantedTable.run.transact(xa).unsafeRun()
  println(s"Create granted wishes: ${createGrantedTableResult}")

  case class UserPass(firstName: String, hashedPassword: String)

  override def logIn(email: String, hash: String): PersistenceResponse[String] = {

    val selectEmail =
      sql"""SELECT email, hashedPassword FROM users WHERE email=$email""".query[UserPass]

    val userPass: Task[Option[UserPass]] =
      selectEmail.option
        .transact(xa)

    val eitherTUserPass: EitherT[Task, String, String] =
      EitherT(userPass.map
      {
        case Some(UserPass(email, hpw)) if hash.isBcrypted(hpw) => Right(email)
        case _ => Left("Bad credentials")
      }
             )

    for
    {
      mail <- eitherTUserPass
      token <- getOrCreateSecretFor(mail)
    } yield
      {
        token
      }
  }

  private def newSecret = java.util.UUID.randomUUID.toString

  private def getOrCreateSecretFor(email: String): PersistenceResponse[String] = {

    val insertOrUpdate =
      sql"""
    INSERT INTO secrets (email, secret, expirationDate)
      VALUES ($email, $newSecret, CURRENT_TIMESTAMP + INTERVAL '15 minutes')
    ON CONFLICT (email) DO UPDATE
      SET expirationDate = CURRENT_TIMESTAMP + INTERVAL '15 minutes'
         """.update.run

    val token: Task[(Int, String)] = ( for
    {
      updateCount <- insertOrUpdate
      token <- sql"SELECT secret FROM secrets WHERE email=$email".query[String].unique
    } yield
        {
          (updateCount, token)
        } ).transact(xa)

    EitherT(token map
            {
              case (1, token) => Right(token)
              case _ => Left(s"Unable to update and retrieve token for $email")
            }
           )
  }


  override def getSecretFor(email: String): PersistenceResponse[String] =
    EitherT(
             sql"SELECT secret FROM secrets WHERE email='$email' and CURRENT_TIMESTAMP < expirationDate"
               .query[String]
               .option
               .map(_.toRight("Token expired"))
               .transact(xa)
           )

  override def getUserFor(secret: String): PersistenceResponse[String] =
    EitherT(
             sql"SELECT email FROM secrets WHERE secret=$secret AND CURRENT_TIMESTAMP < expirationDate"
               .query[String]
               .option
               .map(_.toRight("Token expired"))
               .transact(xa)
           )

  override def emailForSecretURL(secretURL: String): PersistenceResponse[String] =
    EitherT(
             sql"SELECT email FROM users WHERE secretURL=$secretURL"
               .query[String]
               .option
               .map(_.toRight("No user for secret URL"))
               .transact(xa)
           )

  override def getSharingURL(email: String): PersistenceResponse[String] =
    EitherT(
             sql"SELECT secretURL FROM users WHERE email=$email"
               .query[String]
               .option
               .map(_.toRight("User not found"))
               .transact(xa)
           )

  override def userForSecretURL(secret: String): PersistenceResponse[String] =
    EitherT(
             sql"SELECT firstName FROM users WHERE secretURL=$secret"
               .query[String]
               .option
               .map(_.toRight("User not found"))
               .transact(xa)
           )

  override def getEntriesFor(email: String): PersistenceResponse[List[WishEntry]] =
    EitherT(
             sql"SELECT email, heading, description, imageURL, id, index FROM wishes WHERE email=$email"
               .query[WishEntry]
               .list
               .attemptSql
               .map(_.left.map(_.getMessage))
               .transact(xa)
           )

  private def entryToTuple(w: WishEntry): (String, String, String, String, Int) =
    (w.email, w.heading, w.desc, w.image, w.index)

  override def grant(entry: WishEntry): PersistenceResponse[String] =
  EitherT(
    Update[(String, String, String, String, Int)](
          "INSERT INTO granted (email, heading, description, imageURL, index) VALUES (?, ?, ?, ?, ?)"
                                                 )
      .run(entryToTuple(entry)).transact(xa)
      .map
      {
        case 1 => Right("Successfully granted wish")
        case _ => Left("Failed granting wish")
      }
         )

  private def setEntriesFor(email: String,
    entries: List[WishEntry]): Free[ConnectionOp, Either[String, String]] =
    for
    {
      deleteCount <- sql"DELETE FROM wishes WHERE email=$email".update.run
      insertCount <- Update[(String, String, String, String, Int)](
                                                                    "INSERT INTO wishes (email, heading, description," +
                                                                    " imageURL, index) VALUES (?, ?, ?, ?, ?)"
                                                                  )
        .updateMany(entries.map(entryToTuple))
    } yield
      {
        if (insertCount == entries.length)
        {
          Right(s"Successfully updated $insertCount wishes")
        } else
        {
          Left("Failed updating wishes")
        }
      }

  override def set(entries: List[WishEntry], forEmail: String): PersistenceResponse[String] =
    EitherT(setEntriesFor(forEmail, entries).transact(xa))

  override def finalize(registrationToken: String): PersistenceResponse[String] =
    EitherT(
             sql"UPDATE users SET finalized=TRUE WHERE registrationToken=$registrationToken".update.run
               .map(n =>
                      if (n == 1)
                      {
                        Right("Successfully activated user")
                      } else
                      {
                        Left("No user for that token was found")
                      }
                   )
               .transact(xa)
           )

  override def createUser(createUserRequest: CreateUserRequest,
    activationToken: String): PersistenceResponse[String] = {

    val up = Update[DBUser](
                             "insert into users (firstName, lastName, email, hashedPassword, secretURL, registrationToken, " +
                             "finalized) values (?, ?, ?, ?, ?, ?, ?)"
                           )
    val c = createUserRequest
    val dbu = DBUser(c.firstName,
                     c.lastName,
                     c.email,
                     c.password.bcrypt,
                     java.util.UUID.randomUUID.toString,
                     activationToken,
                     false
                    )
    println(s"Creating user: $dbu")
    val insertQuery: ConnectionIO[Int] = up.run(dbu)
    val result = insertQuery.transact(xa)
    EitherT(result.map
    {
      case 1 => Right("Successfully created user")
      case _ => Left("Failed creating user in db")
    }
           )
  }
}
