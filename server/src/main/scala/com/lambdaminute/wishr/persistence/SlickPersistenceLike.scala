package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.model.tags.{RegistrationToken, SecretUrl, WishId}
import shapeless.tag.@@
import shapeless.tag

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import com.github.t3hnar.bcrypt._

trait SlickPersistenceLike extends Persistence[Future, ServiceError] with Tables {

  import profile.api._
  val db: Database
  implicit val ec: ExecutionContext

  override def logIn(user: String, hash: String) = ???

  implicit def futureEitherToEitherT[L, R](f: Future[Either[L, R]]): EitherT[Future, L, R] =
    EitherT(f)

  implicit class RecoverToDbError[T](f: Future[T]) {
    def orError: Future[Either[DatabaseError, T]] =
      f.map(Right.apply).recover {
        case t => Left(DatabaseError(t))
      }
  }

  implicit class RecoverToMissingOrDbError[T](f: Future[Option[T]]) {
    def orMissingValue(name: String): Future[Either[ServiceError, T]] =
      f.map {
          case Some(value) => Right(value)
          case None        => Left(MissingValueError(s"No value found for '$name'"))
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

  def encrypt(str: String): String          = str.bcrypt
  def isHashedAs(str: String, hash: String) = str isBcrypted hash

  override def getRegistrationTokenFor(
      email: String): PersistenceResponse[String @@ RegistrationToken] =
    db.run(
        Users
          .filter(_.email === email)
          .map(_.registrationtoken)
          .result
          .head
      )
      .map(tag[RegistrationToken][String])
      .orError

  override def getUserForSecretUrl(secret: String @@ SecretUrl) = ???

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
      .orMissingValue("email")

  override def getEntriesFor(user: String) = ???

  override def userForSecretURL(secret: String @@ SecretUrl) = ???

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

  override def finalize(registrationToken: String) = ???

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
                          encrypt(password),
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
