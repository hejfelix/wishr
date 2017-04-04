package com.lambdaminute.wishr.component
import chandu0101.scalajs.react.components.Implicits._
import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.model.{LoginRequest, User, Wish}
import com.lambdaminute.wishr.serialization.OptionPickler.write
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}
import scalaz.Alpha.{P, S}

object LoginPage {

  val component = ReactComponentB[Props]("LoginPage")
    .initialState(LoginPage.State("", ""))
    .renderBackend[Backend]
    .build

  def apply(onLogin: Either[String, User] => Unit, goToCreateUserPage: Callback) =
    component(Props(onLogin, goToCreateUserPage))

  case class Props(handleLogin: Either[String, User] => Unit, goToCreateUserPage: Callback)

  case class State(name: String, password: String)

  class Backend($ : BackendScope[Props, State]) {

    def handleNameChange: ReactEventI => Callback =
      e => {
        e.persist()
        $.modState(_.copy(name = e.target.value))
      } >> Callback.info(s"new name: ${e.target.value}")

    def handlePasswordChange: ReactEventI => Callback =
      e => {
        e.persist()
        $.modState(_.copy(password = e.target.value))
      }

    def render(S: State, P: Props) = {

      val sendLogin = Callback {
        println(s"Trying to log in with: ${write[LoginRequest](LoginRequest(S.name, S.password))}")
        Ajax
          .post(s"./login",
                write[LoginRequest](LoginRequest(S.name, S.password)),
                headers = Map("Content-Type" -> "application/json"))
          .map(_.responseText)
          .onComplete {
            case Success(msg) =>
              println(s"Succesfully logged in: $msg")
              val user = User(S.name, msg)
              P.handleLogin(Right(user))
            case Failure(err) =>
              println(s"Error logging in: $err")
              P.handleLogin(Left(err.getMessage))
          }
      }

      def handleLoginButton: ReactEventH => Callback =
        e => sendLogin >> Callback.info("Login button pressed")

      val userNameField: ReactComponentU_ =
        MuiTextField(floatingLabelText = "e-mail", onChange = handleNameChange)()
      val passwordField: ReactComponentU_ =
        MuiTextField(
          floatingLabelText = "password",
          `type` = "password",
          onChange = handlePasswordChange,
          onKeyDown =
            (r: ReactKeyboardEventH) => if (r.key == "Enter") sendLogin else Callback.empty
        )()
      val loginButton =
        MuiFlatButton(label = "login", primary = true, onClick = handleLoginButton)()

      val createNewUserButton = MuiFlatButton(label = "Create a new user",
                                              secondary = true,
                                              onClick = (r: ReactEventH) => P.goToCreateUserPage)()
      val createSection = <.div()

      <.div(
        MuiPaper()(
          <.div(^.cls := "Card",
                <.div(userNameField),
                <.div(passwordField),
                loginButton,
                "or",
                createNewUserButton,
                createSection)))
    }

  }

}
