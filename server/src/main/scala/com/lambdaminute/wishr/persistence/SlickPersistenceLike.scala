package com.lambdaminute.wishr.persistence

import java.time.Instant
import java.time.temporal.ChronoUnit

import cats.data.EitherT
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.model.tags.{RegistrationToken, SecretUrl, SessionToken, WishId}
import shapeless.tag
import shapeless.tag.@@
import cats.implicits._
import cats._
import com.lambdaminute.wishr.auth.Encryption

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

trait SlickPersistenceLike extends Persistence[Future, ServiceError] with Tables {

  import profile.api._
  val db: Database
  implicit val ec: ExecutionContext

  def encryption: Encryption

  override def logIn(email: String,
                     password: String): EitherT[Future, ServiceError, String @@ SessionToken] = {
    val eventualMaybeHash =
      db.run(Users.filter(_.email === email).map(_.hashedpassword).result.headOption)

    val verifiedHash = EitherT(eventualMaybeHash.map {
      case Some(hash) if encryption.isHashedAs(password, hash) =>
        Right(hash)
      case _ =>
        Left(MissingValueError(s"No user found for hash: ${encryption.hash(password)}"))
    })

    for {
      _            <- verifiedHash
      sessionToken <- EitherT(getOrCreateSessionToken(email))
    } yield sessionToken

  }

  private def getOrCreateSessionToken(
      email: String): Future[Either[ServiceError, String @@ SessionToken]] = {
    val expTime     = Instant.now().plus(15, ChronoUnit.MINUTES)
    val stamp       = java.sql.Timestamp.from(expTime)
    val thisSecret  = Secrets.filter(_.email === email)
    val maybeSecret = thisSecret.result.headOption

    val newOrUpdatedSecret = maybeSecret
      .flatMap {
        case Some(_) =>
          thisSecret
            .map(_.expirationdate)
            .update(stamp)
        case None =>
          Secrets += SecretsRow(email, encryption.newToken, stamp)
      }
      .map {
        case 0 => Left(DatabaseError(new Exception(s"Failed to update session token for $email")))
        case n => Right(n)
      }

    val token =
      newOrUpdatedSecret.flatMap {
        case Right(_) =>
          thisSecret.map(_.secret).result.head.map(Right.apply)
        case Left(err) => DBIO.successful(Left(err))
      }

    val query = token.map(_.map(tag[SessionToken][String]))

    db.run(query)
      .recover {
        case t => Left(DatabaseError(t))
      }
  }

  implicit def futureEitherToEitherT[L, R](f: Future[Either[L, R]]): EitherT[Future, L, R] =
    EitherT(f)

  implicit class RecoverToDbError[T](f: Future[T]) {
    def orError: Future[Either[DatabaseError, T]] =
      f.map(Right.apply).recover {
        case t => Left(DatabaseError(t))
      }
  }

  implicit class RecoverToMissingOrDbError[T](f: Future[Option[T]]) {
    def orMissingValue(name: String, value: String): Future[Either[ServiceError, T]] =
      f.map {
          case Some(value) => Right(value)
          case None        => Left(MissingValueError(s"No value found for $name: $value"))
        }
        .recover {
          case t => Left(DatabaseError(t))
        }
  }

  override def getStats(): EitherT[Future, ServiceError, Stats] = {
    val query = for {
      numWishes  <- Wishes.result.map(_.size)
      numGranted <- Wishes.filter(_.granted).result.map(_.size)
      numUsers   <- Users.result.map(_.size)
    } yield Stats(numWishes, numGranted, numUsers)
    db.run(query).orError
  }

  override def getRegistrationTokenFor(
      email: String): PersistenceResponse[String @@ RegistrationToken] =
    db.run(
        Users
          .filter(_.email === email)
          .map(_.registrationtoken)
          .result
          .headOption
      )
      .map(_.map(tag[RegistrationToken][String]))
      .orMissingValue("email", email)

  override def getEmailForSecretUrl(
      secret: String @@ SecretUrl): EitherT[Future, ServiceError, String] =
    db.run(
        Users
          .filter(_.secreturl === (secret: String))
          .map(_.email)
          .result
          .headOption)
      .orMissingValue("secret", secret)

  def wishRowsToEntries(wishes: Seq[WishesRow]): Seq[WishEntry] = wishes.map(wishRowToEntry)
  def wishRowToEntry(row: WishesRow): WishEntry =
    WishEntry(row.email, row.heading, row.description, row.imageurl.mkString, row.index, row.id)

  override def getEntriesForSecret(
      secretUrl: String @@ SecretUrl): PersistenceResponse[Seq[WishEntry]] =
    db.run(
        (for {
          (_, wish) <- (Users.filter(_.secreturl === (secretUrl: String)) joinRight Wishes)
        } yield wish).result
      )
      .map(wishRowsToEntries)
      .orError

  override def getSecretUrl(email: String): PersistenceResponse[String @@ SecretUrl] =
    db.run(
        Users.filter(_.email === email).map(_.secreturl).result.headOption
      )
      .map(_.map(tag[SecretUrl][String]))
      .orMissingValue("email", email)

  override def getEntriesFor(email: String) = {
    val query = (for {
      (_, wish) <- Users.filter(_.email === email) joinRight Wishes
    } yield wish).result.map(wishRowsToEntries)
    db.run(query).orError
  }

  override def emailForSecretURL(
      secret: String @@ SecretUrl): EitherT[Future, ServiceError, String] =
    db.run(Users.filter(_.secreturl === (secret: String)).map(_.email).result.headOption)
      .orMissingValue("secret", secret)

  override def createWish(email: String,
                          heading: String,
                          descr: String,
                          imageUrl: Option[String]): PersistenceResponse[Int @@ WishId] = {
    val newWish = WishesRow(email = email,
                            heading = heading,
                            description = descr,
                            imageurl = imageUrl,
                            index = 0,
                            granted = false,
                            id = 0)
    db.run((Wishes returning Wishes.map(_.id)) += newWish)
      .map(tag[WishId][Int])
      .orError
  }

  override def finalize(
      registrationToken: String @@ RegistrationToken): EitherT[Future, ServiceError, String] = {
    val query = (for {
      user <- Users.filter(_.registrationtoken === (registrationToken: String))
    } yield user.finalized).update(true)
    db.run(query)
      .map {
        case 0 => None
        case _ => Some(registrationToken)
      }
      .orMissingValue("registrationToken", registrationToken)
  }

  override def createUser(firstName: String,
                          lastName: String,
                          email: String,
                          password: String,
                          secretUrl: String @@ SecretUrl,
                          registrationToken: String @@ RegistrationToken) =
    db.run(
        Users += UsersRow(firstName,
                          lastName,
                          email,
                          encryption.hash(password),
                          secretUrl: String,
                          registrationToken: String,
                          false))
      .orError

  override def grant(wishId: Int @@ WishId) = {
    val query = for {
      wish <- Wishes if wish.id === (wishId: Int)
    } yield wish.granted
    db.run(query.update(true)).orError
  }
}
