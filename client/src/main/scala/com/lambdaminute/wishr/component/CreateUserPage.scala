package com.lambdaminute.wishr.component

import cats.Semigroup
import cats.data.Validated.{Invalid, Valid}
import cats.data.{Validated, ValidatedNel}
import chandu0101.scalajs.react.components.Implicits._
import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.model.{CreateUserRequest, Wish}
import japgolly.scalajs.react.vdom.ReactTagOf
import org.scalajs.dom.html.Div

import scala.collection.immutable.Seq
import com.lambdaminute.wishr.serialization.OptionPickler.write
import cats.SemigroupK
import cats.data.NonEmptyList
import cats.syntax.cartesian._
import com.lambdaminute.wishr.component.WishRAppContainer.{Action, Login, Page}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.ext.{Ajax, AjaxException}

import scala.util.{Failure, Success}
import concurrent.ExecutionContext.Implicits.global

object CreateUserPage {

  sealed trait FormError {
    def reason: String
  }
  case class MismatchError(left: String, right: String) extends FormError {
    override def reason = s"$left not the same as $right"
  }
  case class FormatError(reason: String) extends FormError

  def apply(showDialog: (String, Option[Page], List[Action]) => Unit) =
    ReactComponentB[Props]("CreateUserPage")
      .initialState(State())
      .renderBackend[CreateUserPage.Backend]
      .propsDefault(Props(showDialog))

  case class Props(showDialog: (String, Option[Page], List[Action]) => Unit)

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
        case (Some(a), Some(b)) if a == b && a.contains("@") => valid(a)
        case (Some(a), Some(b)) if a != b                    => formMismatchError(a, b)
        case _                                               => formError[String]("Missing e-mail")
      }).toValidatedNel

    def checkPassword(S: State) =
      ((S.password, S.passwordRepeat) match {
        case (Some(a), Some(b)) if a == b && a.length < 6 =>
          formError[String]("Password too short. Must be 6 characters or longer.")
        case (Some(a), Some(b)) if a == b => valid(a)
        case (Some(a), Some(b)) if a != b => formMismatchError("left password", "right password")
        case _                            => formError[String]("Missing password")
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
        case (firstName, lastName, email, password) =>
          CreateUserRequest(firstName, lastName, email, password)
      }

    def render(S: State, P: Props) = {

      def handleInput(s: Option[String] => State => State): ReactEventI => Callback =
        e => {
          e.persist()
          val text = Option(e.target.value)
          $.modState(s(text))
        }

      val firstNameText =
        MuiTextField(floatingLabelText = "First Name",
                     name = "firstname",
                     onChange = handleInput(str => _.copy(firstName = str)))()

      val lastNameText =
        MuiTextField(floatingLabelText = "Last Name",
                     name = "lastname",
                     onChange = handleInput(str => _.copy(lastName = str)))()

      val eMail =
        MuiTextField(floatingLabelText = "e-mail",
                     `type` = "email",
                     onChange = handleInput(str => _.copy(email = str)))()

      val eMailRepeat =
        MuiTextField(floatingLabelText = "repeat e-mail",
                     `type` = "email",
                     onChange = handleInput(str => _.copy(emailRepeat = str)))()

      val password =
        MuiTextField(floatingLabelText = "Password",
                     `type` = "password",
                     onChange = handleInput(str => _.copy(password = str)))()

      val passwordRepeat =
        MuiTextField(floatingLabelText = "Repeat password",
                     `type` = "password",
                     onChange = handleInput(str => _.copy(passwordRepeat = str)))()

      val formValidation = validateForm(S)
      val validationText = formValidation match {
        case Valid(_) => <.div()
        case Invalid(nel) =>
          val divs: List[ReactTagOf[Div]] = nel.toList.map(x => <.div(x.reason))
          <.div(<.h3("Issues"), divs)
      }
      val requestJson = formValidation.toOption

      def submitRequest: Callback =
        Callback({

          requestJson.foreach(json => {
            Ajax
              .post(s"./createuser",
                    write[CreateUserRequest](json),
                    headers = Map("Content-Type" -> "application/json"))
              .onComplete {
                case Success(msg) =>
                  val snackText =
                    s"Succesfully created user. Check your inbox to finalize authorization."
                  P.showDialog(snackText, Option(Login), Nil)
                case Failure(AjaxException(xhr)) =>
                  val snackText =
                    s"Error creating user ${xhr.responseType}: ${xhr.responseText}"
                  P.showDialog(snackText, None, Nil)
                case Failure(err) =>
                  val snackText = s"Error creating user ${err}: ${err.getMessage()}"
                  P.showDialog(snackText, None, Nil)
              }
          })
        })

      val submitButton =
        MuiFlatButton(label = "Submit",
                      primary = true,
                      onClick = (_: ReactEventH) => submitRequest)()

      val form: Seq[ReactTagOf[Div]] =
        List(
          <.div(<.h3("Name"), firstNameText, lastNameText),
          <.div(<.h3("E-mail"), eMail, eMailRepeat),
          <.div(<.h3("Password"), password, passwordRepeat),
          if (formValidation.isValid) <.div(submitButton) else <.div(),
          validationText
        )

      MuiPaper()(<.div(^.cls := "Card", <.h1("Create User"), form))
    }

  }

}
