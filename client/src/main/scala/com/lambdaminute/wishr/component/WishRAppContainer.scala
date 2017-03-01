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
import org.scalajs.dom.ext.{Ajax, AjaxException}
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import chandu0101.scalajs.react.components.Implicits._
import japgolly.scalajs.react.vdom.Frag

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Failure, Success}

object WishRAppContainer {

  sealed trait Page
  case object Login      extends Page
  case object Fetching   extends Page
  case object WishList   extends Page
  case object CreateUser extends Page

  def apply() =
    ReactComponentB[Unit]("WishRAppContainer")
      .initialState(WishRAppContainer.State())
      .renderBackend[WishRAppContainer.Backend]

  case class State(currentPage: Page = Login,
                   userName: Option[String] = None,
                   authorizationSecret: Option[String] = None,
                   theme: MuiTheme = Mui.Styles.getMuiTheme(Mui.Styles.LightRawTheme),
                   errorMessage: Option[String] = None,
                   wishes: List[Wish] = Nil,
                   snackBarText: String = "",
                   snackBarOpen: Boolean = false,
                   dialogText: String = "",
                   dialogOpen: Boolean = false)

  class Backend($ : BackendScope[_, State]) {

    def handleLogin(user: Either[String, User]) {
      user match {
        case Left(msg) =>
          $.modState(_.copy(errorMessage = Option(msg))).runNow()
        case Right(user) =>
          $.modState(
            _.copy(authorizationSecret = Option(user.secret),
                   userName = Option(user.name),
                   currentPage = Fetching)
          ).runNow()
      }
    }

    def render(S: State) = {

      val showSnackBar: (String) => Unit = (withText: String) =>
        $.modState(_.copy(snackBarText = withText, snackBarOpen = true)).runNow()

      def showDialog(withText: String, changePage: Option[Page] = None) =
        changePage match {
          case Some(page) =>
            $.modState(_.copy(dialogText = withText, dialogOpen = true, currentPage = page))
              .runNow()
          case None => $.modState(_.copy(dialogText = withText, dialogOpen = true)).runNow()
        }

      def fetchWishes() =
        Ajax
          .get(s"./entries",
               headers = Map("Content-Type"  -> "application/json",
                             "Authorization" -> S.authorizationSecret.mkString))
          .onComplete {
            case Success(msg) =>
              val wishes = read[List[Wish]](msg.responseText)
              println(s"Got wishes: $wishes")
              $.modState(_.copy(wishes = wishes, currentPage = WishList)).runNow()
            case Failure(AjaxException(xhr)) =>
              println(s"Exception: ${xhr.responseText}")
            case Failure(err) =>
              println(err.getMessage)
          }

      val page: ReactElement = S.currentPage match {
        case CreateUser => CreateUserPage(showDialog).build()
        case Login      => LoginPage(handleLogin, $.modState(_.copy(currentPage = CreateUser))).build()
        case Fetching =>
          fetchWishes()
          <.div("Downloading wishes...")
        case WishList =>
          EditWishesPage(S.userName.mkString,
                         S.wishes,
                         S.theme,
                         S.authorizationSecret.mkString,
                         showSnackBar)
            .build()
      }

      val snackBar = MuiSnackbar(
        autoHideDuration = 2500,
        message = S.snackBarText,
        onRequestClose = (x: String) => $.modState(_.copy(snackBarOpen = false)),
        open = S.snackBarOpen
      )()

      val muiAppBar = MuiAppBar(
        title = "WishR",
        showMenuIconButton = false
      )()

      val dismissDialogButton =
        MuiFlatButton(label = "Ok",
                      onClick = (r: ReactEventH) => $.modState(_.copy(dialogOpen = false)))()
      val dialog = MuiDialog(title = S.dialogText,
                             open = S.dialogOpen,
                             actions = js.Array(dismissDialogButton),
                             onRequestClose =
                               (b: Boolean) => $.modState(_.copy(dialogOpen = false)))()

      MuiMuiThemeProvider(muiTheme = S.theme)(<.div(muiAppBar, page, snackBar, dialog))
    }

  }
}
