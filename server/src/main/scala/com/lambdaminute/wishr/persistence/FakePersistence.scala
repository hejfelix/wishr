package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import cats.effect.Effect
import com.lambdaminute.wishr.model.{Stats, WishEntry}
import com.lambdaminute.wishr.model.tags._

class FakePersistence[F[_]](implicit override val F: Effect[F]) extends Persistence[F, String] {

  override def getSessionToken(email: Email): Unit = ???

  override def logIn(email: String, password: String): PersistenceResponse[SessionToken] = ???

  override def getStats(): PersistenceResponse[Stats] = ???

  override def getRegistrationTokenFor(email: String): PersistenceResponse[RegistrationToken] = ???

  override def getEmailForSecretUrl(secret: SecretUrl): PersistenceResponse[String] = ???

  override def getEntriesForSecret(secretURL: SecretUrl): PersistenceResponse[Seq[WishEntry]] = ???

  override def getSecretUrl(email: String): PersistenceResponse[SecretUrl] = ???

  override def getEntriesFor(email: String): PersistenceResponse[Seq[WishEntry]] = ???

  override def emailForSessionToken(token: SessionToken): PersistenceResponse[Email] =
    EitherT.pure[F, String]("fake-e-mail".asEmail)

  override def emailForSecretURL(secret: SecretUrl): PersistenceResponse[String] = ???

  override def createWish(email: String,
                          heading: String,
                          descr: String,
                          imageUrl: Option[String]): PersistenceResponse[WishId] = ???

  override def finalize(registrationToken: RegistrationToken): PersistenceResponse[String] = ???

  override def createUser(firstName: String,
                          lastName: String,
                          email: String,
                          password: String,
                          secretUrl: SecretUrl,
                          registrationToken: RegistrationToken): PersistenceResponse[Int] = ???

  override def grant(wishId: WishId): PersistenceResponse[Int] = ???
}
