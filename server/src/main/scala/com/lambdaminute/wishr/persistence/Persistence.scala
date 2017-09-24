package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import com.lambdaminute.wishr.model.tags.{RegistrationToken, SecretUrl, SessionToken, WishId}
import com.lambdaminute.wishr.model.{CreateUserRequest, Stats, WishEntry}
import shapeless.tag.@@

trait Persistence[F[_], Error] {

  type PersistenceResponse[T] = EitherT[F, Error, T]

  def logIn(email: String, password: String): PersistenceResponse[String @@ SessionToken]

  def getStats(): PersistenceResponse[Stats]

  def getRegistrationTokenFor(email: String): PersistenceResponse[String @@ RegistrationToken]

  def getEmailForSecretUrl(secret: String @@ SecretUrl): PersistenceResponse[String]

  def getEntriesForSecret(secretURL: String @@ SecretUrl): PersistenceResponse[Seq[WishEntry]]

  def getSecretUrl(email: String): PersistenceResponse[String @@ SecretUrl]

  def getEntriesFor(email: String): PersistenceResponse[Seq[WishEntry]]

  def emailForSecretURL(secret: String @@ SecretUrl): PersistenceResponse[String]

  def createWish(email: String,
                 heading: String,
                 descr: String,
                 imageUrl: Option[String]): PersistenceResponse[Int @@ WishId]

  def finalize(registrationToken: String @@ RegistrationToken): PersistenceResponse[String]

  def createUser(firstName: String,
                 lastName: String,
                 email: String,
                 password: String,
                 secretUrl: String @@ SecretUrl,
                 registrationToken: String @@ RegistrationToken): PersistenceResponse[Int]

  def grant(wishId: Int @@ WishId): PersistenceResponse[Int]
}
