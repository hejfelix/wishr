//scalastyle:off
package com.lambdaminute.wishr.model
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.PostgresProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Secrets.schema ++ Users.schema ++ Wishes.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Secrets
   *  @param email Database column email SqlType(varchar)
   *  @param secret Database column secret SqlType(varchar)
   *  @param expirationdate Database column expirationdate SqlType(timestamp) */
  case class SecretsRow(email: String, secret: String, expirationdate: java.sql.Timestamp)
  /** GetResult implicit for fetching SecretsRow objects using plain SQL queries */
  implicit def GetResultSecretsRow(implicit e0: GR[String], e1: GR[java.sql.Timestamp]): GR[SecretsRow] = GR{
    prs => import prs._
    SecretsRow.tupled((<<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table secrets. Objects of this class serve as prototypes for rows in queries. */
  class Secrets(_tableTag: Tag) extends profile.api.Table[SecretsRow](_tableTag, "secrets") {
    def * = (email, secret, expirationdate) <> (SecretsRow.tupled, SecretsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(email), Rep.Some(secret), Rep.Some(expirationdate)).shaped.<>({r=>import r._; _1.map(_=> SecretsRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column email SqlType(varchar) */
    val email: Rep[String] = column[String]("email")
    /** Database column secret SqlType(varchar) */
    val secret: Rep[String] = column[String]("secret")
    /** Database column expirationdate SqlType(timestamp) */
    val expirationdate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("expirationdate")

    /** Foreign key referencing Users (database name secrets_email_fkey) */
    lazy val usersFk = foreignKey("secrets_email_fkey", email, Users)(r => r.email, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Uniqueness Index over (email) (database name secrets_email_key) */
    val index1 = index("secrets_email_key", email, unique=true)
  }
  /** Collection-like TableQuery object for table Secrets */
  lazy val Secrets = new TableQuery(tag => new Secrets(tag))

  /** Entity class storing rows of table Users
   *  @param firstname Database column firstname SqlType(varchar)
   *  @param lastname Database column lastname SqlType(varchar)
   *  @param email Database column email SqlType(varchar), PrimaryKey
   *  @param hashedpassword Database column hashedpassword SqlType(varchar)
   *  @param secreturl Database column secreturl SqlType(varchar)
   *  @param registrationtoken Database column registrationtoken SqlType(varchar)
   *  @param finalized Database column finalized SqlType(bool) */
  case class UsersRow(firstname: String, lastname: String, email: String, hashedpassword: String, secreturl: String, registrationtoken: String, finalized: Boolean)
  /** GetResult implicit for fetching UsersRow objects using plain SQL queries */
  implicit def GetResultUsersRow(implicit e0: GR[String], e1: GR[Boolean]): GR[UsersRow] = GR{
    prs => import prs._
    UsersRow.tupled((<<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Boolean]))
  }
  /** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends profile.api.Table[UsersRow](_tableTag, "users") {
    def * = (firstname, lastname, email, hashedpassword, secreturl, registrationtoken, finalized) <> (UsersRow.tupled, UsersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(firstname), Rep.Some(lastname), Rep.Some(email), Rep.Some(hashedpassword), Rep.Some(secreturl), Rep.Some(registrationtoken), Rep.Some(finalized)).shaped.<>({r=>import r._; _1.map(_=> UsersRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column firstname SqlType(varchar) */
    val firstname: Rep[String] = column[String]("firstname")
    /** Database column lastname SqlType(varchar) */
    val lastname: Rep[String] = column[String]("lastname")
    /** Database column email SqlType(varchar), PrimaryKey */
    val email: Rep[String] = column[String]("email", O.PrimaryKey)
    /** Database column hashedpassword SqlType(varchar) */
    val hashedpassword: Rep[String] = column[String]("hashedpassword")
    /** Database column secreturl SqlType(varchar) */
    val secreturl: Rep[String] = column[String]("secreturl")
    /** Database column registrationtoken SqlType(varchar) */
    val registrationtoken: Rep[String] = column[String]("registrationtoken")
    /** Database column finalized SqlType(bool) */
    val finalized: Rep[Boolean] = column[Boolean]("finalized")
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))

  /** Entity class storing rows of table Wishes
   *  @param email Database column email SqlType(varchar)
   *  @param heading Database column heading SqlType(varchar)
   *  @param description Database column description SqlType(varchar)
   *  @param imageurl Database column imageurl SqlType(varchar), Default(None)
   *  @param index Database column index SqlType(int4)
   *  @param granted Database column granted SqlType(bool)
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey */
  case class WishesRow(email: String, heading: String, description: String, imageurl: Option[String] = None, index: Int, granted: Boolean, id: Int)
  /** GetResult implicit for fetching WishesRow objects using plain SQL queries */
  implicit def GetResultWishesRow(implicit e0: GR[String], e1: GR[Option[String]], e2: GR[Int], e3: GR[Boolean]): GR[WishesRow] = GR{
    prs => import prs._
    WishesRow.tupled((<<[String], <<[String], <<[String], <<?[String], <<[Int], <<[Boolean], <<[Int]))
  }
  /** Table description of table wishes. Objects of this class serve as prototypes for rows in queries. */
  class Wishes(_tableTag: Tag) extends profile.api.Table[WishesRow](_tableTag, "wishes") {
    def * = (email, heading, description, imageurl, index, granted, id) <> (WishesRow.tupled, WishesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(email), Rep.Some(heading), Rep.Some(description), imageurl, Rep.Some(index), Rep.Some(granted), Rep.Some(id)).shaped.<>({r=>import r._; _1.map(_=> WishesRow.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column email SqlType(varchar) */
    val email: Rep[String] = column[String]("email")
    /** Database column heading SqlType(varchar) */
    val heading: Rep[String] = column[String]("heading")
    /** Database column description SqlType(varchar) */
    val description: Rep[String] = column[String]("description")
    /** Database column imageurl SqlType(varchar), Default(None) */
    val imageurl: Rep[Option[String]] = column[Option[String]]("imageurl", O.Default(None))
    /** Database column index SqlType(int4) */
    val index: Rep[Int] = column[Int]("index")
    /** Database column granted SqlType(bool) */
    val granted: Rep[Boolean] = column[Boolean]("granted")
    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
  }
  /** Collection-like TableQuery object for table Wishes */
  lazy val Wishes = new TableQuery(tag => new Wishes(tag))
}
