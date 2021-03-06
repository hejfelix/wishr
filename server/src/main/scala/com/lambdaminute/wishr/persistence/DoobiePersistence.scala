package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import cats.effect.Effect
import cats.implicits._
import com.github.t3hnar.bcrypt._
import com.lambdaminute.wishr.config.DBConfig
import com.lambdaminute.wishr.model.tags.{Password => WishrPassword, _}
import com.lambdaminute.wishr.model.{Stats, Wish, WishEntry}
import com.lambdaminute.wishr.persistence
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import org.postgresql.util.PGInterval

import concurrent.duration._
import scala.concurrent.duration.FiniteDuration

class DoobiePersistence[F[_]](dbconf: DBConfig, tokenTimeout: FiniteDuration)(
    override implicit val F: Effect[F])
    extends Persistence[F, String] {

  val xa = Transactor.fromDriverManager[F](
    driver = "org.postgresql.Driver",
    url =
      s"jdbc:postgresql://${dbconf.url}?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory",
    user = dbconf.user,
    pass = dbconf.password
  )

  def finiteDurationToPostGresInterval(duration: FiniteDuration): String =
    s"${duration.toMinutes} minutes"

  private val postgresTokenTimeout: PGInterval = new PGInterval(
    finiteDurationToPostGresInterval(tokenTimeout))
  implicit val metainterzval = Meta.other[PGInterval](schemaH = "interval")
//
//  val createUsersTable: Update0 =
//    sql"""
//    CREATE TABLE IF NOT EXISTS users (
//      firstName          VARCHAR,
//      lastName           VARCHAR,
//      email              VARCHAR UNIQUE,
//      hashedPassword     VARCHAR,
//      secretURL          VARCHAR,
//      registrationToken  VARCHAR,
//      finalized          BOOLEAN
//      )
//  """.update
//
//  val createSecretsTable: Update0 =
//    sql"""
//    CREATE TABLE IF NOT EXISTS secrets (
//       email            VARCHAR UNIQUE,
//       secret           VARCHAR,
//       expirationDate   TIMESTAMP
//      )
//   """.update
//
//  val createWishesTable: Update0 =
//    sql"""
//    CREATE TABLE IF NOT EXISTS wishes (
//       email                    VARCHAR,
//       heading                  VARCHAR,
//       description              VARCHAR,
//       imageURL                 VARCHAR,
//       index                    INTEGER,
//       id                       SERIAL
//      )
//   """.update
//
//  val createGrantedTable: Update0 =
//    sql"""
//    CREATE TABLE IF NOT EXISTS granted (
//       email                    VARCHAR,
//       heading                  VARCHAR,
//       description              VARCHAR,
//       imageURL                 VARCHAR,
//       index                    INTEGER,
//       id                       SERIAL
//      )
//   """.update
//
//  val createUsersResult: Int = createUsersTable.run.transact(xa).unsafeRun()
//  println(s"Create users: $createUsersResult")
//  val createSecretsResult: Int = createSecretsTable.run.transact(xa).unsafeRun()
//  println(s"Create secrets: $createSecretsResult")
//  val createWishesResult: Int = createWishesTable.run.transact(xa).unsafeRun()
//  println(s"Create wishes: $createWishesResult")
//  val createGrantedTableResult: Int = createGrantedTable.run.transact(xa).unsafeRun()
//  println(s"Create granted wishes: ${createGrantedTableResult}")
//  println(s"Create granted wishes: ${createGrantedTableResult}")
//  println(s"Create granted wishes: ${createGrantedTableResult}")
//  println(s"Create granted wishes: ${createGrantedTableResult}")
//
  case class UserPass(firstName: String, hashedPassword: String)

  override def getUserInfo(token: SessionToken): PersistenceResponse[persistence.DBUser] =
    EitherT(sql"""

          SELECT firstname, lastname, users.email, secreturl FROM users, secrets
            WHERE users.email=secrets.email AND secrets.secret=${token.toString}

          """.query[DBUser].unique.transact(xa).map(Either.right[String, DBUser]))

  override def getUserInfoFromSecret(token: SecretUrl): PersistenceResponse[persistence.DBUser] =
    EitherT(sql"""

          SELECT firstname, lastname, users.email, secreturl FROM users, secrets
            WHERE secrets.email=users.email AND users.secreturl=${token.toString}

          """.query[DBUser].unique.transact(xa).map(Either.right[String, DBUser]))

  override def logIn(email: Email, password: WishrPassword): PersistenceResponse[SessionToken] = {

    val selectEmail =
      sql"""SELECT email, hashedPassword FROM users WHERE lower(email)=lower(${email.toString})"""
        .query[UserPass]

    val userPass: F[Option[UserPass]] =
      selectEmail.option
        .transact(xa)

    val eitherTUserPass: EitherT[F, String, String] =
      EitherT(userPass.map {
        case Some(UserPass(email, hpw)) if password.isBcrypted(hpw) => Right(email)
        case _                                                      => Left("Bad credentials")
      })

    for {
      mail  <- eitherTUserPass
      token <- getOrCreateSecretFor(mail)
    } yield {
      token.asSessionToken
    }
  }

  def getStats(): PersistenceResponse[Stats] =
    EitherT(
      sql"""
         SELECT  (
                 SELECT COUNT(*)
                 FROM   wishes
           |     WHERE granted=false
                 ) AS numberOfWishes,
                 (
                 SELECT COUNT(*)
                 FROM   wishes
                 WHERE granted=true
                 ) AS numberOfGranted,
                 (
                 SELECT COUNT(*)
                 FROM   users
                 ) AS numberOfUsers
          """
        .query[Stats]
        .option
        .map(_.toRight("Table missing for stats"))
        .transact(xa))

  private def newSecret = java.util.UUID.randomUUID.toString

  private def getOrCreateSecretFor(email: String): PersistenceResponse[String] = {
    val insertOrUpdate: ConnectionIO[Int] =
      sql"""
    INSERT INTO secrets (email, secret, expirationDate)
      VALUES ($email, $newSecret, CURRENT_TIMESTAMP + $postgresTokenTimeout)
    ON CONFLICT (email) DO UPDATE
      SET expirationDate = CURRENT_TIMESTAMP + $postgresTokenTimeout
         """.update.run

    val token: F[(Int, String)] = (for {
      updateCount <- insertOrUpdate
      token       <- sql"SELECT secret FROM secrets WHERE email=$email".query[String].unique
    } yield {
      (updateCount, token)
    }).transact(xa)

    EitherT(token map {
      case (1, token) => Right(token)
      case _          => Left(s"Unable to update and retrieve token for $email")
    })
  }

  override def emailForSessionToken(secret: SessionToken): PersistenceResponse[Email] =
    EitherT(
      sql"SELECT email FROM secrets WHERE secret=${secret.toString} AND CURRENT_TIMESTAMP < expirationDate"
        .query[String]
        .option
        .map(_.toRight("Token expired"))
        .transact(xa)
    ).map(_.asEmail)

  override def emailForSecretURL(secretURL: SecretUrl): PersistenceResponse[Email] =
    EitherT(
      sql"SELECT email FROM users WHERE secretURL=${secretURL.toString}"
        .query[String]
        .option
        .map(_.toRight("No user for secret URL"))
        .transact(xa)
    ).map(_.asEmail)

  override def getSecretUrl(email: Email): PersistenceResponse[SecretUrl] =
    EitherT(
      sql"SELECT secretURL FROM users WHERE email=${email.toString}"
        .query[String]
        .option
        .map(_.toRight("User not found"))
        .transact(xa)
    ).map(_.asSecretUrl)

  override def getEntriesFor(email: Email): PersistenceResponse[List[WishEntry]] =
    EitherT(
      sql"SELECT email, heading, description, imageURL, index, id FROM wishes WHERE email=${email.toString} AND granted=FALSE"
        .query[WishEntry]
        .to[List]
        .attemptSql
        .map(_.left.map(_.getMessage))
        .transact(xa)
    )

  override def grant(id: WishId): PersistenceResponse[Int] =
    EitherT(
      sql"UPDATE wishes SET granted = true WHERE id = ${id.toString}".update.run
        .transact(xa)
        .map {
          case 1 => Right(1)
          case _ => Left("Failed granting wish")
        }
    )

  override def finalize(registrationToken: RegistrationToken): PersistenceResponse[String] =
    EitherT(
      sql"UPDATE users SET finalized=TRUE WHERE registrationToken=${registrationToken.toString}".update.run
        .map(n =>
          if (n == 1) {
            Right("Successfully activated user")
          } else {
            Left("No user for that token was found")
        })
        .transact(xa)
    )

  override def createUser(firstName: String,
                          lastName: String,
                          email: Email,
                          password: WishrPassword,
                          secretUrl: SecretUrl,
                          registrationToken: RegistrationToken): PersistenceResponse[Int] = {

    val hash      = password.bcrypt
    val secretUrl = java.util.UUID.randomUUID.toString
    val up        = sql"""

    INSERT INTO users (firstName, lastName, email, hashedPassword, secretURL, registrationToken, finalized)
      VALUES ($firstName, $lastName, ${email.toString}, $hash, $secretUrl, ${registrationToken.toString}, false)

      """.update

    EitherT(up.run.transact(xa).map {
      case 1 => Right(1)
      case _ => Left("Failed creating user in db")
    })
  }

  override def getRegistrationTokenFor(email: Email): PersistenceResponse[RegistrationToken] =
    EitherT(
      sql"SELECT registrationToken FROM users WHERE email=${email.toString}"
        .query[String]
        .unique
        .transact(xa)
        .map(_.asRegistrationToken)
        .map(Either.right)
    )

  override def getSessionToken(email: Email): PersistenceResponse[SessionToken] = EitherT(
    sql"SELECT secret FROM secrets, users WHERE users.email=${email.toString} AND secrets.email=users.email"
      .query[String]
      .unique
      .transact(xa)
      .map(_.asSessionToken)
      .map(Either.right)
  )

  override def getEmailForSecretUrl(secret: SecretUrl): PersistenceResponse[String] = EitherT(
    sql"SELECT email FROM users WHERE secretURL=${secret.toString}"
      .query[String]
      .unique
      .transact(xa)
      .map(Either.right[String, String])
      .recover {
        case e: Throwable => Either.left(e.getMessage)
      }
  )

  override def updateWish(wishEntry: Wish): PersistenceResponse[Unit] =
    EitherT {
      val w = wishEntry
      sql"""

          UPDATE wishes SET  heading=${w.heading}, description=${w.desc}, imageURL=${w.image}
            WHERE id=${w.id}

          """.update.run.transact(xa).map {
        case 1 => Right(())
        case _ => Left(s"Failed updating wish $w")
      }
    }

  override def swapWishIndices(i: WishId, j: WishId): PersistenceResponse[(Int, Int)] = {
    println("WHAT IS GOING ON????")
    ???
  }

  override def getEntriesForSecret(secretURL: SecretUrl): PersistenceResponse[List[WishEntry]] =
    EitherT(sql"""

          SELECT users.email, heading, description, imageURL, index, id FROM users, wishes
            WHERE secretURL=${secretURL.toString} AND users.email=wishes.email AND granted=FALSE

          """.query[WishEntry].to[List].transact(xa).map(Right.apply))

  override def createWish(email: Email,
                          heading: String,
                          descr: String,
                          imageUrl: Option[String]): PersistenceResponse[WishId] = {
    val query: fs2.Stream[F, Int] = sql"""

      INSERT INTO wishes (email, heading, description, imageurl, index)
        VALUES (${email.toString}, ${heading.toString}, ${descr.toString}, ${imageUrl.mkString}, 0) RETURNING id

        """.update
      .withGeneratedKeys[Int]("id")
      .transact(xa)

    EitherT(
      query.compile.foldMonoid
        .map(_.asWishId)
        .map(Right.apply)
    )
  }

  override def deleteWish(id: WishId): PersistenceResponse[WishId] =
    EitherT(sql"""
      
      DELETE FROM wishes WHERE id=${id.toInt}
      
      """.update.run.transact(xa).map(_ => id.asRight[String]))

}
