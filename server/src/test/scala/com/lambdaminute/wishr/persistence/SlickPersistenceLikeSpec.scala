package com.lambdaminute.wishr.persistence

import cats.data.EitherT
import com.lambdaminute.wishr.model.{ServiceError, Stats}
import com.lambdaminute.wishr.model.tags.SecretTag
import org.scalatest.{AsyncWordSpec, EitherValues, Matchers, WordSpec}
import shapeless.tag.@@

import cats.implicits._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random
import concurrent.duration._

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

  def newPersistence: Persistence[Future, ServiceError, String @@ SecretTag] =
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
                                    activationToken = "")
        _     <- persistence.createWish("", "", "", None)
        id    <- persistence.createWish("", "", "", None)
        _     <- persistence.grant(id)
        stats <- persistence.getStats()
      } yield stats

      val expected = Stats(numberOfWishes = 2, numberOfGranted = 1, numberOfUsers = 1)
      stats.value.map(_.right.value shouldBe expected)
    }
  }

}
