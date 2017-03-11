package com.lambdaminute.wishr.persistence
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import cats.data.EitherT
import com.github.t3hnar.bcrypt._
import com.lambdaminute.wishr.model.{CreateUserRequest, WishEntry}
import fs2.interop.cats._
import fs2.{Strategy, Task}
import org.h2.tools.Server
import slick.dbio.DBIOAction
import slick.dbio.Effect.{Read, Schema, Write}
import slick.jdbc.H2Profile.api._
import slick.jdbc.meta.MTable
import slick.sql.FixedSqlAction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class UsersSlickTable(tag: Tag) extends Table[DBUser](tag, "users") {
  def firstName         = column[String]("firstName")
  def lastName          = column[String]("lastName")
  def email             = column[String]("email", O.PrimaryKey)
  def hashedPassword    = column[String]("hashedPassword")
  def secretURL         = column[String]("secretURL")
  def registrationToken = column[String]("registrationToken")
  def finalized         = column[Boolean]("finalized")

  def * =
    (firstName, lastName, email, hashedPassword, secretURL, registrationToken, finalized) <> (DBUser.tupled, DBUser.unapply)
}

case class DBSecret(email: String, secret: String, expirationDate: Timestamp)
class SecretsSlickTable(tag: Tag) extends Table[DBSecret](tag, "secrets") {
  def email          = column[String]("email", O.PrimaryKey)
  def secret         = column[String]("secret")
  def expirationDate = column[Timestamp]("expirationDate")

  def * = (email, secret, expirationDate) <> (DBSecret.tupled, DBSecret.unapply)
}

class WishesSlickTable(tag: Tag)
    extends Table[(String, String, String, String, Int, Int)](tag, "wishes") {
  def email       = column[String]("email")
  def heading     = column[String]("heading")
  def description = column[String]("description")
  def imageURL    = column[String]("imageURL")
  def index       = column[Int]("index")
  def id          = column[Int]("id", O.AutoInc)

  def * = (email, heading, description, imageURL, index, id)
}

//class TestTable(tag: Tag) extends Table[(String, Boolean)](tag, "Test") {
//  def test = column[String]("test", O.Unique, O.PrimaryKey)
//  def bool = column[Boolean]("bool")
//
//  def * = (test, bool)
//}

case class SlickPersistence() extends Persistence[String, String] {

  implicit def futureToEitherTTask[L, R](f: Future[Either[L, R]]): EitherT[Task, L, R] =
    EitherT(f.toTask)

  implicit val fs2Strategy = Strategy.fromExecutionContext(global)
  implicit class F2T[T](future: Future[T]) {
    def toTask: Task[T] =
      fs2.Task.async(register =>
        future.onComplete {
          case Success(m) => register(Right(m))
          case Failure(m) => register(Left(m))
      })
  }

  val h2Server = Server.createTcpServer("-tcpAllowOthers").start()

  val db = Database
    .forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver", keepAliveConnection = true)

  val users   = TableQuery[UsersSlickTable]
  val secrets = TableQuery[SecretsSlickTable]
  val wishes  = TableQuery[WishesSlickTable]

  val tables = List(users, secrets, wishes)

  //Create tables if they don't exist
//  (for {
//    existingTables <- db.run(MTable.getTables)
//    names = existingTables.map(_.name.name)
//    createIfNotExist = tables
//      .filter(table => (!names.contains(table.baseTableRow.tableName)))
//      .map(_.schema.create)
//    result <- db.run(DBIO.sequence(createIfNotExist))
//  } yield result).onComplete {
//    case Success(msg) => println(s"Created tables: $msg")
//    case Failure(err) => println(s"Failed creating tables:${err.getMessage}")
//  }

  val create: DBIOAction[Unit, NoStream, Schema] =
    DBIO.seq((users.schema ++ secrets.schema ++ wishes.schema).create)
  db.run(create)
    .onComplete {
      case Success(msg) => println(s"Created tables: $msg")
      case Failure(err) => println(s"Failed creating tables:${err.getMessage}")
    }

  db.run(MTable.getTables).onComplete {
    case Success(m) => println(m)
    case Failure(m) => println(m)
  }

  override def logIn(user: String, hash: String): PersistenceResponse[String] = {
    val email =
      users.filter(u => u.email === user).map(u => (u.email, u.hashedPassword))

    val futureEmail = db.run(email.result.headOption).map {
      case Some((email, hashedPW)) if hash.isBcrypted(hashedPW) => Right(email)
      case _                                                    => Left("Bad credentials")
    }

    for {
      mail  <- (futureEmail: PersistenceResponse[String])
      token <- getOrCreateSecretFor(mail)
    } yield token
  }

  private def getOrCreateSecretFor(email: String): PersistenceResponse[String] = {

    val newStamp = Timestamp.from(Instant.now().plus(15, ChronoUnit.MINUTES))

    val insertOrUpdate = for {
      rowsAffected <- secrets.filter(_.email === email).map(_.expirationDate).update(newStamp)
      result <- rowsAffected match {
        case 0 => secrets += DBSecret(email, UUID.randomUUID.toString, newStamp)
        case 1 => DBIO.successful(1)
        case _ =>
          DBIO.failed(new RuntimeException(s"Too many rows updated while updating auth token"))
      }
    } yield result

    val token: DBIOAction[(Int, Option[String]), NoStream, Write with Write with Read] = for {
      updateCount <- insertOrUpdate
      token       <- secrets.filter(_.email === email).map(_.secret).result.headOption
    } yield (updateCount, token)

    db.run(token) map {
      case (1, Some(token)) => Right(token)
      case _                => Left(s"Unable to update and retrieve token for $email")
    }
  }

  override def getSecretFor(user: String): PersistenceResponse[String] = ???

  override def getUserFor(secret: String): PersistenceResponse[String] = ???

  override def emailForSecretURL(secretURL: String): PersistenceResponse[String] = ???

  override def getSharingURL(email: String): PersistenceResponse[String] = ???

  override def getEntriesFor(user: String): PersistenceResponse[List[WishEntry]] = ???

  override def set(entries: List[WishEntry]): PersistenceResponse[String] = ???

  override def finalize(registrationToken: String): PersistenceResponse[String] = {

    val update: FixedSqlAction[Int, NoStream, Write] = (for {
      user <- users if user.registrationToken === registrationToken && user.finalized === false
    } yield user.finalized).update(true)

    db.run(update)
      .map {
        case 1 => Right("Successfully activated user")
        case 0 => Left("No user for that token was found")
      }
  }

  override def createUser(createUserRequest: CreateUserRequest,
                          activationToken: String): PersistenceResponse[String] = {
    val c = createUserRequest
    val newUserEntry = DBUser(c.firstName,
                              c.lastName,
                              c.email,
                              c.password.bcrypt,
                              UUID.randomUUID.toString,
                              activationToken,
                              false)
    val insert = users += newUserEntry

    db.run(insert map {
      case 1 => Right(activationToken)
      case 0 => Left("Unable to create user")
    })
  }
}
