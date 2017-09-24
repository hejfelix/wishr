package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import cats.implicits._
import com.lambdaminute.wishr.model.tags.{RegistrationToken, SecretUrl}
import com.lambdaminute.wishr.model.{ServiceError, Stats}
import org.scalatest.{AsyncWordSpec, EitherValues, Matchers}
import shapeless.tag

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

case class H2SlickPersistence(url: String) extends SlickPersistenceLike {

  override implicit val ec: ExecutionContext = concurrent.ExecutionContext.Implicits.global
  override val profile                       = slick.jdbc.H2Profile

  import profile.api._
  override val db: Database = Database.forURL(url, driver = "org.h2.Driver")

  val dbio = for {
    _ <- schema.create
  } yield ()

  Await.result(db.run(dbio.withPinnedSession), 10.minutes)
}

class SlickPersistenceLikeSpec extends AsyncWordSpec with Matchers with EitherValues {

  private val rnd = new Random(1337)

  def newPersistence: Persistence[Future, ServiceError] =
    H2SlickPersistence(
      s"jdbc:h2:mem:codesdb${rnd.nextInt};MODE=PostgreSQL;DB_CLOSE_DELAY=10;DATABASE_TO_UPPER=false")

  "Slick persistence" should {

    "Get stats" in {
      var persistence = newPersistence

      val stats: EitherT[Future, ServiceError, Stats] = for {
        _ <- persistence.createUser(firstName = "",
                                    lastName = "",
                                    email = "",
                                    password = "",
                                    secretUrl = tag[SecretUrl](""),
                                    registrationToken = tag[RegistrationToken](""))
        _     <- persistence.createWish("", "", "", None)
        id    <- persistence.createWish("", "", "", None)
        _     <- persistence.grant(id)
        stats <- persistence.getStats()
      } yield stats

      val expected = Stats(numberOfWishes = 2, numberOfGranted = 1, numberOfUsers = 1)
      stats.value.map(_.right.value shouldBe expected)
    }

    "Create user with registration token" in {
      val persistence = newPersistence
      val mail        = "hej@be.com"
      val regToken    = tag[RegistrationToken](java.util.UUID.randomUUID().toString)
      val secret = for {
        _ <- persistence.createUser(firstName = "",
                                    lastName = "",
                                    email = mail,
                                    password = "",
                                    secretUrl = tag[SecretUrl](""),
                                    registrationToken = regToken)
        token <- persistence.getRegistrationTokenFor(mail)
      } yield token

      secret.value.map(_.right.value shouldBe regToken)
    }

    "Create user with secret url" in {
      val persistence = newPersistence
      val mail        = "hej@be.com"
      val secretUrl   = tag[SecretUrl](java.util.UUID.randomUUID().toString)
      val secret = for {
        _ <- persistence.createUser(firstName = "",
                                    lastName = "",
                                    email = mail,
                                    password = "",
                                    secretUrl = secretUrl,
                                    registrationToken = tag[RegistrationToken](""))
        token <- persistence.getSecretUrl(mail)
      } yield token

      secret.value.map(_.right.value shouldBe secretUrl)
    }

    "Fail to get secret for non existing user" in {
      newPersistence.getSecretUrl("").value.map(_.left.value.msg shouldBe "No value found for 'email'")
    }

  }

}
