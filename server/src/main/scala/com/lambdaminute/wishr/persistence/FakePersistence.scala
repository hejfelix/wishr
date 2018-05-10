package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import cats.effect.Effect
import com.lambdaminute.wishr.model.{Stats, WishEntry}
import com.lambdaminute.wishr.model.tags._

class FakePersistence[F[_]](implicit override val F: Effect[F]) extends Persistence[F, String] {

  override def getSessionToken(email: Email): PersistenceResponse[SessionToken] = ???

  override def logIn(email: Email, password: Password): PersistenceResponse[SessionToken] = ???

  override def getStats(): PersistenceResponse[Stats] = ???

  override def getRegistrationTokenFor(email: Email): PersistenceResponse[RegistrationToken] = ???

  override def getEmailForSecretUrl(secret: SecretUrl): PersistenceResponse[String] = ???

  override def getEntriesForSecret(secretURL: SecretUrl): PersistenceResponse[List[WishEntry]] = ???

  override def getSecretUrl(email: Email): PersistenceResponse[SecretUrl] = ???

  override def getEntriesFor(email: Email): PersistenceResponse[List[WishEntry]] = ???

  override def emailForSessionToken(token: SessionToken): PersistenceResponse[Email] =
    EitherT.pure[F, String]("fake-e-mail".asEmail)

  override def emailForSecretURL(secret: SecretUrl): PersistenceResponse[Email] = ???

  override def createWish(email: Email,
                          heading: String,
                          descr: String,
                          imageUrl: Option[String]): PersistenceResponse[WishId] = ???

  override def finalize(registrationToken: RegistrationToken): PersistenceResponse[String] = ???

  override def createUser(firstName: String,
                          lastName: String,
                          email: Email,
                          password: Password,
                          secretUrl: SecretUrl,
                          registrationToken: RegistrationToken): PersistenceResponse[Int] = ???

  override def grant(wishId: WishId): PersistenceResponse[Int] = ???

  override def updateWish(wishEntry: WishEntry): PersistenceResponse[Unit] = ???

  override def swapWishIndices(i: WishId, j: WishId): PersistenceResponse[(Int, Int)] = ???
}
