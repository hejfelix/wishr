package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import com.lambdaminute.wishr.model.{CreateUserRequest, Stats, WishEntry}

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

  def set(entries: List[WishEntry], forEmail: String): PersistenceResponse[String]

  def finalize(registrationToken: String): PersistenceResponse[String]

  def createUser(createUserRequest: CreateUserRequest,
                 activationToken: String): PersistenceResponse[String]

  def grant(entry: WishEntry): PersistenceResponse[String]
}
