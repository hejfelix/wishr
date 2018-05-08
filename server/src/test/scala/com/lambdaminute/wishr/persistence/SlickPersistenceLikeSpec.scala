package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import cats.implicits._
import com.lambdaminute.wishr.auth.Encryption
import com.lambdaminute.wishr.model.tags.{RegistrationToken, SecretUrl}
import com.lambdaminute.wishr.model.{ServiceError, Stats}
import org.scalatest.{AsyncWordSpec, EitherValues, Matchers}
import shapeless.tag

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

//case class H2SlickPersistence(url: String, override val encryption: Encryption)
//    extends SlickPersistenceLike {
//
//  override implicit val ec: ExecutionContext = concurrent.ExecutionContext.Implicits.global
//  override val profile                       = slick.jdbc.H2Profile
//
//  import profile.api._
//  override val db: Database = Database.forURL(url, driver = "org.h2.Driver")
//
//  val dbio = for {
//    _ <- schema.create
//  } yield ()
//
//  Await.result(db.run(dbio.withPinnedSession), 10.minutes)
//}

case object MockEncryption extends Encryption {
  override def newToken                              = ???
  override def hash(s: String)                       = ???
  override def isHashedAs(str: String, hash: String) = ???
}

class SlickPersistenceLikeSpec extends AsyncWordSpec with Matchers with EitherValues {

//  private val rnd = new Random(1337)
//
////  def newPersistence(encryption: Encryption = MockEncryption): Persistence[Future, ServiceError] =
////    H2SlickPersistence(
////      s"jdbc:h2:mem:codesdb${rnd.nextInt};MODE=PostgreSQL;DB_CLOSE_DELAY=10;DATABASE_TO_UPPER=false",
////      encryption)
//
//  "Slick persistence" should {
//
//    "Get stats" in {
//      val enc = new Encryption {
//        override def newToken: String                               = "42"
//        override def isHashedAs(str: String, hash: String): Boolean = ???
//        override def hash(s: String): String                        = "1337"
//      }
//      var persistence = newPersistence(enc)
//
//      val stats: EitherT[Future, ServiceError, Stats] = for {
//        _ <- persistence.createUser(firstName = "",
//                                    lastName = "",
//                                    email = "",
//                                    password = "",
//                                    secretUrl = tag[SecretUrl](""),
//                                    registrationToken = tag[RegistrationToken](""))
//        _     <- persistence.createWish("", "", "", None)
//        id    <- persistence.createWish("", "", "", None)
//        _     <- persistence.grant(id)
//        stats <- persistence.getStats()
//      } yield stats
//
//      val expected = Stats(numberOfWishes = 2, numberOfGranted = 1, numberOfUsers = 1)
//      stats.value.map(_.right.value shouldBe expected)
//    }
//
//    "Create user with registration token" in {
//      val enc = new Encryption {
//        override def newToken: String                               = ???
//        override def isHashedAs(str: String, hash: String): Boolean = ???
//        override def hash(s: String): String                        = "1337"
//      }
//      val persistence = newPersistence(enc)
//      val mail        = "hej@be.com"
//      val regToken    = tag[RegistrationToken](java.util.UUID.randomUUID().toString)
//      val secret = for {
//        _ <- persistence.createUser(firstName = "",
//                                    lastName = "",
//                                    email = mail,
//                                    password = "",
//                                    secretUrl = tag[SecretUrl](""),
//                                    registrationToken = regToken)
//        token <- persistence.getRegistrationTokenFor(mail)
//      } yield token
//
//      secret.value.map(_.right.value shouldBe regToken)
//    }
//
//    "Create user with secret url" in {
//      val enc = new Encryption {
//        override def newToken: String                               = ???
//        override def isHashedAs(str: String, hash: String): Boolean = ???
//        override def hash(s: String): String                        = "1337"
//      }
//      val persistence = newPersistence(enc)
//      val mail        = "hej@be.com"
//      val secretUrl   = tag[SecretUrl](java.util.UUID.randomUUID().toString)
//      val secret = for {
//        _ <- persistence.createUser(firstName = "",
//                                    lastName = "",
//                                    email = mail,
//                                    password = "",
//                                    secretUrl = secretUrl,
//                                    registrationToken = tag[RegistrationToken](""))
//        token <- persistence.getSecretUrl(mail)
//      } yield token
//
//      secret.value.map(_.right.value shouldBe secretUrl)
//    }
//
//    "Log in user with password" in {
//      val sessionToken = java.util.UUID.randomUUID().toString
//      val enc = new Encryption {
//        override def newToken: String =
//          sessionToken
//        override def isHashedAs(str: String, hash: String): Boolean = true
//        override def hash(s: String): String                        = "1337"
//      }
//      val persistence = newPersistence(enc)
//      val mail        = "hej@be.com"
//      val firstName   = "Hej"
//      val lastName    = "Abe"
//      val password    = "s3cr37"
//      val secretUrl   = tag[SecretUrl](java.util.UUID.randomUUID().toString)
//      val login = for {
//        _ <- persistence.createUser(firstName = firstName,
//                                    lastName = lastName,
//                                    email = mail,
//                                    password = password,
//                                    secretUrl = secretUrl,
//                                    registrationToken = tag[RegistrationToken](""))
//        token <- persistence.logIn(mail, password)
//      } yield token
//
//      login.value.map(_.right.value shouldBe sessionToken)
//    }
//
//    "Not log in user with invalid password url" in {
//      val sessionToken = java.util.UUID.randomUUID().toString
//      val enc = new Encryption {
//        override def newToken: String =
//          sessionToken
//        override def isHashedAs(str: String, hash: String): Boolean = false
//        override def hash(s: String): String                        = "1337"
//      }
//      val persistence     = newPersistence(enc)
//      val mail            = "hej@be.com"
//      val firstName       = "Hej"
//      val lastName        = "Abe"
//      val password        = "s3cr37"
//      val secretUrl       = tag[SecretUrl](java.util.UUID.randomUUID().toString)
//      val invalidPassword = "Wrong Password"
//      val login = for {
//        _ <- persistence.createUser(firstName = firstName,
//                                    lastName = lastName,
//                                    email = mail,
//                                    password = password,
//                                    secretUrl = secretUrl,
//                                    registrationToken = tag[RegistrationToken](""))
//        fullName <- persistence.logIn(mail, invalidPassword)
//      } yield fullName
//
//      login.value.map(_.left.value.msg should startWith("No user found for hash: "))
//    }
//
//    "Fail to get registration secret for non existing user" in {
//      val email = "dont@exist.com"
//      newPersistence()
//        .getRegistrationTokenFor(email)
//        .value
//        .map(_.left.value.msg shouldBe s"No value found for email: $email")
//    }
//
//    "Fail to get secret url for non existing user" in {
//      val email = "dont@exist.com"
//      newPersistence()
//        .getSecretUrl(email)
//        .value
//        .map(_.left.value.msg shouldBe s"No value found for email: $email")
//    }

//  }

}
