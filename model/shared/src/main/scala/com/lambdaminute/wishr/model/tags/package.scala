package com.lambdaminute.wishr.model

import io.circe.{Decoder, Encoder}
import shapeless.tag.@@

package object tags {

  object Tags {
    trait RegistrationToken
    trait SessionToken
    trait SecretUrl
    trait WishId
    trait Password
    trait Email
  }

  type RegistrationToken = String @@ Tags.RegistrationToken
  type SessionToken      = String @@ Tags.SessionToken
  type SecretUrl         = String @@ Tags.SecretUrl
  type Email             = String @@ Tags.Email
  type Password          = String @@ Tags.Password
  type WishId            = Int @@ Tags.WishId

  implicit class StringTaggable(s: String) {
    def asRegistrationToken: RegistrationToken = shapeless.tag[Tags.RegistrationToken][String](s)
    def asSessionToken: SessionToken           = shapeless.tag[Tags.SessionToken][String](s)
    def asSecretUrl: SecretUrl                 = shapeless.tag[Tags.SecretUrl][String](s)
    def asEmail: Email                         = shapeless.tag[Tags.Email][String](s)
    def asPassword: Password                   = shapeless.tag[Tags.Password][String](s)
  }

  implicit val emailDecoder: Decoder[Email]        = Decoder[String].map(_.asEmail)
  implicit val passwoordDecoder: Decoder[Password] = Decoder[String].map(_.asPassword)
  implicit val emailEncoder: Encoder[Email]        = Encoder.apply[String].contramap[Email](identity)
  implicit val passwordEncoder: Encoder[Password] =
    Encoder.apply[String].contramap[Password](identity)

  implicit class IntTaggable(i: Int) {
    def asWishId: WishId = shapeless.tag[Tags.WishId][Int](i)
  }

}
