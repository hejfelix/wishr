package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import cats.effect.Effect
import com.lambdaminute.wishr.model.tags.{Email, _}
import com.lambdaminute.wishr.model._

trait Persistence[F[_], Error] {

  implicit val F: Effect[F]

  type PersistenceResponse[T] = EitherT[F, Error, T]

  def getSessionToken(email: Email)

  def logIn(email: String, password: String): PersistenceResponse[SessionToken]

  def getStats(): PersistenceResponse[Stats]

  def getRegistrationTokenFor(email: String): PersistenceResponse[RegistrationToken]

  def getEmailForSecretUrl(secret: SecretUrl): PersistenceResponse[String]

  def getEntriesForSecret(secretURL: SecretUrl): PersistenceResponse[Seq[WishEntry]]

  def getSecretUrl(email: String): PersistenceResponse[SecretUrl]

  def getEntriesFor(email: String): PersistenceResponse[Seq[WishEntry]]

  def emailForSessionToken(token: SessionToken): PersistenceResponse[Email]

  def emailForSecretURL(secret: SecretUrl): PersistenceResponse[String]

  def createWish(email: String,
                 heading: String,
                 descr: String,
                 imageUrl: Option[String]): PersistenceResponse[WishId]

  def finalize(registrationToken: RegistrationToken): PersistenceResponse[String]

  def createUser(firstName: String,
                 lastName: String,
                 email: String,
                 password: String,
                 secretUrl: SecretUrl,
                 registrationToken: RegistrationToken): PersistenceResponse[Int]

  def grant(wishId: WishId): PersistenceResponse[Int]
}
