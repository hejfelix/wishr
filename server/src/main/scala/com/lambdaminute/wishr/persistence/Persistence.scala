package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import com.lambdaminute.wishr.model.tags.WishId
import com.lambdaminute.wishr.model.{CreateUserRequest, Stats, WishEntry}
import shapeless.tag.@@

trait Persistence[F[_], Error, Secret] {

  type PersistenceResponse[T] = EitherT[F, Error, T]

  def logIn(user: String, hash: String): PersistenceResponse[String]

  def getStats(): PersistenceResponse[Stats]

  def getSecretFor(user: String): PersistenceResponse[Secret]

  def getUserFor(secret: String): PersistenceResponse[String]

  def emailForSecretURL(secretURL: String): PersistenceResponse[String]

  def getSharingURL(email: String): PersistenceResponse[String]

  def getEntriesFor(user: String): PersistenceResponse[List[WishEntry]]

  def userForSecretURL(secret: String): PersistenceResponse[String]

  def createWish(email: String,
                 heading: String,
                 descr: String,
                 imageUrl: Option[String]): PersistenceResponse[Int @@ WishId]

  def finalize(registrationToken: String): PersistenceResponse[String]

  def createUser(firstName: String,
                 lastName: String,
                 email: String,
                 password: String,
                 activationToken: String): PersistenceResponse[Int]

  def grant(wishId: Int @@ WishId): PersistenceResponse[Int]
}
