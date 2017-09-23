package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.model.tags.{SecretTag, WishId}
import shapeless.tag.@@
import shapeless.tag

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import com.github.t3hnar.bcrypt._

trait SlickPersistenceLike
    extends Persistence[Future, ServiceError, String @@ SecretTag]
    with Tables {

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

  override def getSecretFor(user: String) = ???

  override def getUserFor(secret: String) = ???

  override def emailForSecretURL(secretURL: String) = ???

  override def getSharingURL(email: String) = ???

  override def getEntriesFor(user: String) = ???

  override def userForSecretURL(secret: String) = ???

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
                          activationToken: String) =
    db.run(
        Users += UsersRow(firstName,
                          lastName,
                          email,
                          encrypt(password),
                          java.util.UUID.randomUUID().toString,
                          activationToken,
                          false))
      .orError

  override def grant(wishId: Int @@ WishId) = {
    val query = for {
      wish <- Wishes if wish.id === (wishId: Int)
    } yield wish.granted
    db.run(query.update(true)).orError
  }
}
