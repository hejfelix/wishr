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

  def apply(onLogin: Either[String, User] => Unit) =
    ReactComponentB[Props]("LoginPage")
      .initialState(LoginPage.State("", ""))
      .renderBackend[LoginPage.Backend]
      .propsDefault(Props(onLogin))

  case class Props(handleLogin: Either[String, User] => Unit)

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
      } >> Callback.info(s"new password: ${e.target.value}")

    def render(S: State, P: Props) = {

      val sendLogin = Callback {
        Ajax
          .post(s"./login",
                write[LoginRequest](LoginRequest(S.name, S.password)),
                headers = Map("Content-Type" ->
                  "application/json"))
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

      val userNameField: ReactComponentU_ = MuiTextField(hintText = "username", onChange = handleNameChange)()
      val passwordField: ReactComponentU_ =
        MuiTextField(hintText = "password", `type` = "password", onChange = handlePasswordChange)()
      val loginButton = MuiFlatButton(label = "login", onClick = handleLoginButton)()
      <.div(<.div(userNameField), <.div(passwordField), loginButton)
    }

  }

}
