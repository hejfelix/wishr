package com.lambdaminute.wishr.persistence

import com.github.t3hnar.bcrypt._
import com.lambdaminute.wishr.model.{CreateUserRequest, WishEntry}
import doobie.free.connection.ConnectionIO
import doobie.imports._
import org.h2.tools.Server

case class InMemoryH2(dbConfigKey: String, port: Int) extends Persistence[String, String] {

  val h2Server = Server.createTcpServer("-tcpAllowOthers").start()

//  val xa = DriverManagerTransactor[IOLite]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")
  val xa = DriverManagerTransactor[IOLite](
    "org.h2.Driver",
    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    "sa",
    ""
  )

  val create: Update0 =
    sql"""
    CREATE TABLE users (
      firstName       VARCHAR,
      lastName        VARCHAR,
      email           VARCHAR     UNIQUE,
      hashedPassword   VARCHAR,
      registrationToken VARCHAR,
      finalized  BOOLEAN
      )
  """.update

  val createResult: Int = create.run.transact(xa).unsafePerformIO
  println(s"Createresult: $createResult")

  case class UserPass(firstName: String, hashedPassword: String)

  override def logIn(user: String, hash: String): Either[String, String] = {
    val selectUser =
      sql"""SELECT firstName, hashedPassword FROM users WHERE firstName=$user""".query[UserPass]

    println(s"Trying password: $hash against db...")

    val userPass: List[UserPass] = selectUser.list.transact(xa).unsafePerformIO
    userPass match {
      case UserPass(_, storedHash) :: Nil if hash.isBcrypted(storedHash) => Right("fake token")
      case _ => Left("Bad user credentials")
    }
  }

  override def getSecretFor(user: String): Either[String, String] = ???

  override def getUserFor(secret: String): Either[String, String] = ???

  override def getEntriesFor(user: String): Either[String, List[WishEntry]] = ???

  override def set(entries: List[WishEntry]): Either[String, String] = ???

  override def finalize(registrationToken: String): Either[String, String] = ???

  override def createUser(createUserRequest: CreateUserRequest,
                          activationToken: String): Either[String, String] = {
    val up = Update[DBUser](
      "insert into users (firstName, lastName, email, hashedPassword, registrationToken, " +
        "finalized) values (?, ?, ?, ?, ?, ?)")
    val c                              = createUserRequest
    val dbu                            = DBUser(c.firstName, c.lastName, c.email, c.password.bcrypt, activationToken, false)
    val insertQuery: ConnectionIO[Int] = up.run(dbu)
    val result: Int = insertQuery.transact(xa).unsafePerformIO
    result match {
      case 1 => Right("Successfully created user")
      case _ => Left("Failed creating user in db")
    }
  }
}
