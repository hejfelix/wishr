package com.lambdaminute.wishr.persistence

import com.lambdaminute.wishr.model.{CreateUserRequest, WishEntry}

trait Persistence[Error,Secret] {

  def logIn(user: String, hash: String): Either[Error, String]

  def getSecretFor(user: String): Either[Error, Secret]

  def getUserFor(secret: String): Either[Error, String]

  def getEntriesFor(user: String): Either[Error, List[WishEntry]]

  def set(entries: List[WishEntry]): Either[Error, String]

  def finalize(registrationToken: String): Either[Error, String]

  def createUser(createUserRequest: CreateUserRequest, activationToken: String): Either[Error, String]
}
