package com.lambdaminute.wishr.persistence

import com.lambdaminute.wishr.config.DBConfig
import com.lambdaminute.wishr.model.{CreateUserRequest, WishEntry}


case class H2Persistence[F[_]](dBConfig: DBConfig) extends Persistence[F, String, String] {
  override def logIn(user: String, hash: String) = ???

  override def getStats() = ???

  override def getSecretFor(user: String) = ???

  override def getUserFor(secret: String) = ???

  override def emailForSecretURL(secretURL: String) = ???

  override def getSharingURL(email: String) = ???

  override def getEntriesFor(user: String) = ???

  override def userForSecretURL(secret: String) = ???

  override def set(entries: List[WishEntry], forEmail: String) = ???

  override def finalize(registrationToken: String) = ???

  override def createUser(createUserRequest: CreateUserRequest, activationToken: String) = ???

  override def grant(entry: WishEntry) = ???
}
