package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import cats.effect.Effect
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.model.tags.{Email, _}

trait Persistence[F[_], PersistenceError] {

  implicit val F: Effect[F]

  type PersistenceResponse[T] = EitherT[F, PersistenceError, T]

  /*
      Registration stuff
   */
  def getRegistrationTokenFor(email: Email): PersistenceResponse[RegistrationToken]

  def finalize(registrationToken: RegistrationToken): PersistenceResponse[String]

  def createUser(firstName: String,
                 lastName: String,
                 email: Email,
                 password: Password,
                 secretUrl: SecretUrl,
                 registrationToken: RegistrationToken): PersistenceResponse[Int]

  /*
      Account stuff
   */

  def getSessionToken(email: Email): PersistenceResponse[SessionToken]

  def emailForSessionToken(token: SessionToken): PersistenceResponse[Email]

  def logIn(email: Email, password: Password): PersistenceResponse[SessionToken]

  def getEmailForSecretUrl(secret: SecretUrl): PersistenceResponse[String]

  def getSecretUrl(email: Email): PersistenceResponse[SecretUrl]

  def getUserInfo(token: SessionToken): PersistenceResponse[DBUser]

  def getUserInfoFromSecret(secretUrl: SecretUrl): PersistenceResponse[DBUser]

  /*
      Wishes stuff
   */
  def updateWish(wishEntry: Wish): PersistenceResponse[Unit]

  def swapWishIndices(i: WishId, j: WishId): PersistenceResponse[(Int, Int)]

  def emailForSecretURL(secret: SecretUrl): PersistenceResponse[Email]

  def getStats(): PersistenceResponse[Stats]

  def getEntriesForSecret(secretURL: SecretUrl): PersistenceResponse[List[WishEntry]]

  def getEntriesFor(email: Email): PersistenceResponse[List[WishEntry]]

  def createWish(email: Email,
                 heading: String,
                 descr: String,
                 imageUrl: Option[String]): PersistenceResponse[WishId]

  def grant(id: WishId): PersistenceResponse[Int]

  def deleteWish(id: WishId): PersistenceResponse[WishId]
}
