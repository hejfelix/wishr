package com.lambdaminute.wishr.component

import cats.Semigroup
import cats.data.{Validated, ValidatedNel}
import chandu0101.scalajs.react.components.Implicits._
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react.vdom.ReactTagOf
import org.scalajs.dom.html.Div

import scala.collection.immutable.Seq
//import com.lambdaminute.wishr.serialization.OptionPickler.write
import cats.SemigroupK
import cats.data.NonEmptyList
import cats.syntax.cartesian._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

object CreateUserPage {

  sealed trait FormError
  case class MismatchError(left: String, right: String) extends FormError
  case class FormatError(reason: String)                extends FormError

  def apply() =
    ReactComponentB[Props]("CreateUserPage")
      .initialState(State())
      .renderBackend[CreateUserPage.Backend]
      .propsDefault(Props())

  case class CreateUserRequest(firstName: String,
                               lastName: String,
                               email: String,
                               password: String)
  case class Props()

  case class State(
      firstName: Option[String] = None,
      lastName: Option[String] = None,
      email: Option[String] = None,
      emailRepeat: Option[String] = None,
      password: Option[String] = None,
      passwordRepeat: Option[String] = None
  )

  class Backend($ : BackendScope[Props, State]) {

    def checkName(S: State): ValidatedNel[FormError, String] =
      S.firstName
        .fold(formError[String]("First name not defined")) {
          case name if name.length < 1 => formError("First name too short")
          case name                    => valid(name)
        }
        .toValidatedNel

    def checkLastName(S: State): ValidatedNel[FormError, String] =
      (S.lastName match {
        case Some(name) if name.length < 1 => formError[String]("Last name too short")
        case None                          => formError[String]("Last name not defined")
        case Some(name)                    => valid(name)
      }).toValidatedNel

    def checkEmail(S: State) =
      ((S.email, S.emailRepeat) match {
        case (Some(a),Some(b)) if a == b && a.contains("@") => valid(a)
        case (Some(a),Some(b)) if a != b => formMismatchError(a,b)
        case _ => formError[String]("Missing e-mail")
      }).toValidatedNel

    def checkPassword(S: State) =
      ((S.password, S.passwordRepeat) match {
        case (Some(a),Some(b)) if a == b && a.length < 6 =>
          formError[String]("Password too short. Must be 6 characters or longer.")
        case (Some(a),Some(b)) if a == b => valid(a)
        case (Some(a),Some(b)) if a != b => formMismatchError(a,b)
        case _ => formError[String]("Missing password")
      }).toValidatedNel

    private def valid(s: String) = Validated.Valid(s)

    private def formError[T](s: String): Validated[FormError, T] =
      Validated.Invalid(FormatError(s))

    private def formMismatchError(a: String, b: String): Validated[FormError, Nothing] =
      Validated.Invalid(MismatchError(a, b))

    def validateNames(S: State) = checkName(S)

    implicit val nelSemigroup: Semigroup[NonEmptyList[FormError]] =
      SemigroupK[NonEmptyList].algebra[FormError]

    def validateForm(S: State) =
      (checkName(S) |@| checkLastName(S) |@| checkEmail(S) |@| checkPassword(S)).map {
        case (firstName, lastName, email, password) => CreateUserRequest(firstName, lastName, email, password)
      }

    def render(S: State, P: Props) = {

      def handleInput(s: Option[String] => State => State): ReactEventI => Callback =
        e => {
          e.persist()
          val text = Option(e.target.value)
          $.modState(s(text))
        }

      val firstNameText =
        MuiTextField(hintText = "First Name",
                     onChange = handleInput(str => _.copy(firstName = str)))()

      val lastNameText =
        MuiTextField(hintText = "Last Name",
                     onChange = handleInput(str => _.copy(lastName = str)))()

      val eMail =
        MuiTextField(hintText = "e-mail", onChange = handleInput(str => _.copy(email = str)))()

      val eMailRepeat =
        MuiTextField(hintText = "repeat e-mail",
                     onChange = handleInput(str => _.copy(emailRepeat = str)))()

      val password =
        MuiTextField(hintText = "Password",
                     `type` = "password",
                     onChange = handleInput(str => _.copy(password = str)))()

      val passwordRepeat =
        MuiTextField(hintText = "Repeat password",
                     `type` = "password",
                     onChange = handleInput(str => _.copy(passwordRepeat = str)))()

      val submitButton =
        MuiFlatButton(label = "Submit", onClick = (_: ReactEventH) => Callback(println(s"${S}")))()

      val formIsValid: ValidatedNel[FormError, CreateUserRequest] = validateForm(S)

      val form: Seq[ReactTagOf[Div]] =
        List(
          <.div(<.h3("Name"), firstNameText, lastNameText),
          <.div(<.h3("E-mail"), eMail, eMailRepeat),
          <.div(<.h3("Password"), password, passwordRepeat),
          <.div(submitButton),
          <.div(formIsValid.toString)
        )

      MuiPaper()(<.div(<.h1("Create User"), form))
    }

  }

}
