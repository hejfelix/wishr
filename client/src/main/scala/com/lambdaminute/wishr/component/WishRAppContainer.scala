package com.lambdaminute.wishr.component

import chandu0101.scalajs.react.components.materialui.{
  Mui,
  MuiFlatButton,
  MuiMuiThemeProvider,
  MuiPaper,
  MuiTheme,
  ZDepth
}
import com.lambdaminute.wishr.component.LoginPage.Props
import com.lambdaminute.wishr.model.{User, Wish}
import com.lambdaminute.wishr.serialization.OptionPickler.read
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^.{<, ^}
import org.scalajs.dom.ext.Ajax
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import chandu0101.scalajs.react.components.Implicits._
import japgolly.scalajs.react.vdom.Frag

import scala.concurrent.ExecutionContext.Implicits.global

object WishRAppContainer {

  sealed trait Page
  case object Login    extends Page
  case object Fetching extends Page
  case object WishList extends Page

  def apply() =
    ReactComponentB[Unit]("WishRAppContainer")
      .initialState(WishRAppContainer.State())
      .renderBackend[WishRAppContainer.Backend]

  case class State(currentPage: Page = Login,
                   userName: Option[String] = None,
                   authorizationSecret: Option[String] = None,
                   theme: MuiTheme = Mui.Styles.getMuiTheme(Mui.Styles.LightRawTheme),
                   errorMessage: Option[String] = None,
                   wishes: List[Wish] = Nil)

  class Backend($ : BackendScope[_, State]) {

    def handleLogin(user: Either[String, User]) {
      user match {
        case Left(msg) =>
          $.modState(_.copy(errorMessage = Option(msg))).runNow()
        case Right(user) =>
          $.modState(
            _.copy(authorizationSecret = Option(user.secret), userName = Option(user.name), currentPage = Fetching)
          ).runNow()
      }
    }

    def render(S: State) = {

      def fetchWishes =
        Ajax
          .get(s"./entries",
               headers = Map("Content-Type" -> "application/json", "Authorization" -> S.authorizationSecret.mkString))
          .foreach { xhr =>
            val wishes = read[List[Wish]](xhr.responseText)
            println(s"Got wishes: $wishes")
            $.modState(_.copy(wishes = wishes, currentPage = WishList)).runNow()
          }

      val page: ReactElement = S.currentPage match {
        case Login => LoginPage(handleLogin).build()
        case Fetching =>
          fetchWishes
          <.div("Downloading wishes...")
        case WishList => EditWishesPage(S.userName.mkString, S.wishes, S.theme, S.authorizationSecret.mkString).build()
      }

      MuiMuiThemeProvider(muiTheme = S.theme)(page)
    }

  }
}
