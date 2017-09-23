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
   *  @param email Database column email SqlType(varchar), Default(None)
   *  @param secret Database column secret SqlType(varchar), Default(None)
   *  @param expirationdate Database column expirationdate SqlType(timestamp), Default(None) */
  case class SecretsRow(email: Option[String] = None, secret: Option[String] = None, expirationdate: Option[java.sql.Timestamp] = None)
  /** GetResult implicit for fetching SecretsRow objects using plain SQL queries */
  implicit def GetResultSecretsRow(implicit e0: GR[Option[String]], e1: GR[Option[java.sql.Timestamp]]): GR[SecretsRow] = GR{
    prs => import prs._
    SecretsRow.tupled((<<?[String], <<?[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table secrets. Objects of this class serve as prototypes for rows in queries. */
  class Secrets(_tableTag: Tag) extends profile.api.Table[SecretsRow](_tableTag, "secrets") {
    def * = (email, secret, expirationdate) <> (SecretsRow.tupled, SecretsRow.unapply)

    /** Database column email SqlType(varchar), Default(None) */
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Default(None))
    /** Database column secret SqlType(varchar), Default(None) */
    val secret: Rep[Option[String]] = column[Option[String]]("secret", O.Default(None))
    /** Database column expirationdate SqlType(timestamp), Default(None) */
    val expirationdate: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("expirationdate", O.Default(None))

    /** Foreign key referencing Users (database name secrets_email_fkey) */
    lazy val usersFk = foreignKey("secrets_email_fkey", email, Users)(r => Rep.Some(r.email), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Uniqueness Index over (email) (database name secrets_email_key) */
    val index1 = index("secrets_email_key", email, unique=true)
  }
  /** Collection-like TableQuery object for table Secrets */
  lazy val Secrets = new TableQuery(tag => new Secrets(tag))

  /** Entity class storing rows of table Users
   *  @param firstname Database column firstname SqlType(varchar), Default(None)
   *  @param lastname Database column lastname SqlType(varchar), Default(None)
   *  @param email Database column email SqlType(varchar), PrimaryKey
   *  @param hashedpassword Database column hashedpassword SqlType(varchar), Default(None)
   *  @param secreturl Database column secreturl SqlType(varchar), Default(None)
   *  @param registrationtoken Database column registrationtoken SqlType(varchar), Default(None)
   *  @param finalized Database column finalized SqlType(bool), Default(None) */
  case class UsersRow(firstname: Option[String] = None, lastname: Option[String] = None, email: String, hashedpassword: Option[String] = None, secreturl: Option[String] = None, registrationtoken: Option[String] = None, finalized: Option[Boolean] = None)
  /** GetResult implicit for fetching UsersRow objects using plain SQL queries */
  implicit def GetResultUsersRow(implicit e0: GR[Option[String]], e1: GR[String], e2: GR[Option[Boolean]]): GR[UsersRow] = GR{
    prs => import prs._
    UsersRow.tupled((<<?[String], <<?[String], <<[String], <<?[String], <<?[String], <<?[String], <<?[Boolean]))
  }
  /** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends profile.api.Table[UsersRow](_tableTag, "users") {
    def * = (firstname, lastname, email, hashedpassword, secreturl, registrationtoken, finalized) <> (UsersRow.tupled, UsersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (firstname, lastname, Rep.Some(email), hashedpassword, secreturl, registrationtoken, finalized).shaped.<>({r=>import r._; _3.map(_=> UsersRow.tupled((_1, _2, _3.get, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column firstname SqlType(varchar), Default(None) */
    val firstname: Rep[Option[String]] = column[Option[String]]("firstname", O.Default(None))
    /** Database column lastname SqlType(varchar), Default(None) */
    val lastname: Rep[Option[String]] = column[Option[String]]("lastname", O.Default(None))
    /** Database column email SqlType(varchar), PrimaryKey */
    val email: Rep[String] = column[String]("email", O.PrimaryKey)
    /** Database column hashedpassword SqlType(varchar), Default(None) */
    val hashedpassword: Rep[Option[String]] = column[Option[String]]("hashedpassword", O.Default(None))
    /** Database column secreturl SqlType(varchar), Default(None) */
    val secreturl: Rep[Option[String]] = column[Option[String]]("secreturl", O.Default(None))
    /** Database column registrationtoken SqlType(varchar), Default(None) */
    val registrationtoken: Rep[Option[String]] = column[Option[String]]("registrationtoken", O.Default(None))
    /** Database column finalized SqlType(bool), Default(None) */
    val finalized: Rep[Option[Boolean]] = column[Option[Boolean]]("finalized", O.Default(None))
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))

  /** Entity class storing rows of table Wishes
   *  @param email Database column email SqlType(varchar), Default(None)
   *  @param heading Database column heading SqlType(varchar), Default(None)
   *  @param description Database column description SqlType(varchar), Default(None)
   *  @param imageurl Database column imageurl SqlType(varchar), Default(None)
   *  @param index Database column index SqlType(int4), Default(None)
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey */
  case class WishesRow(email: Option[String] = None, heading: Option[String] = None, description: Option[String] = None, imageurl: Option[String] = None, index: Option[Int] = None, id: Int)
  /** GetResult implicit for fetching WishesRow objects using plain SQL queries */
  implicit def GetResultWishesRow(implicit e0: GR[Option[String]], e1: GR[Option[Int]], e2: GR[Int]): GR[WishesRow] = GR{
    prs => import prs._
    WishesRow.tupled((<<?[String], <<?[String], <<?[String], <<?[String], <<?[Int], <<[Int]))
  }
  /** Table description of table wishes. Objects of this class serve as prototypes for rows in queries. */
  class Wishes(_tableTag: Tag) extends profile.api.Table[WishesRow](_tableTag, "wishes") {
    def * = (email, heading, description, imageurl, index, id) <> (WishesRow.tupled, WishesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (email, heading, description, imageurl, index, Rep.Some(id)).shaped.<>({r=>import r._; _6.map(_=> WishesRow.tupled((_1, _2, _3, _4, _5, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column email SqlType(varchar), Default(None) */
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Default(None))
    /** Database column heading SqlType(varchar), Default(None) */
    val heading: Rep[Option[String]] = column[Option[String]]("heading", O.Default(None))
    /** Database column description SqlType(varchar), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column imageurl SqlType(varchar), Default(None) */
    val imageurl: Rep[Option[String]] = column[Option[String]]("imageurl", O.Default(None))
    /** Database column index SqlType(int4), Default(None) */
    val index: Rep[Option[Int]] = column[Option[Int]]("index", O.Default(None))
    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
  }
  /** Collection-like TableQuery object for table Wishes */
  lazy val Wishes = new TableQuery(tag => new Wishes(tag))
}
