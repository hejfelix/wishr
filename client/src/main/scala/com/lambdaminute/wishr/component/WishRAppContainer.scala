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
import com.lambdaminute.wishr.serialization.OptionPickler.{read, write}
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

  case class Action(title: String, onClick: Callback)

  case class State(currentPage: Page = Login,
                   userName: Option[String] = None,
                   authorizationSecret: Option[String] = None,
                   theme: MuiTheme = Mui.Styles.getMuiTheme(Mui.Styles.LightRawTheme),
                   errorMessage: Option[String] = None,
                   wishes: List[Wish] = Nil,
                   snackBarText: String = "",
                   snackBarOpen: Boolean = false,
                   dialogText: String = "",
                   dialogOpen: Boolean = false,
                   dialogActions: List[Action] = Nil,
                   editingWishes: List[Wish] = Nil)

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

      def showSnackBar: (String) => Unit =
        (withText: String) =>
          $.modState(_.copy(snackBarText = withText, snackBarOpen = true)).runNow()

      def startEditing(w: Wish) =
        $.modState(s => s.copy(editingWishes = w :: s.editingWishes)).runNow()

      def changeWish(from: Wish, to: Wish)(wishes: List[Wish]): List[Wish] =
        wishes.takeWhile(_ != from) ++ (to :: wishes.dropWhile(_ != from).drop(1))

      def dropFirstMatch[T](l: List[T], t: T) =
        l.takeWhile(_ != t) ++ l.dropWhile(_ != t).drop(1)

      def stopEditing(w: Wish, newState: Wish) =
        $.modState(
          s =>
            persist(s.copy(editingWishes = dropFirstMatch(s.editingWishes, w),
                           wishes = changeWish(w, newState)(s.wishes))))
          .runNow()

      def persist(state: State): State = {
        Ajax
          .post(s"./set",
                write[List[Wish]](state.wishes),
                headers = Map("Content-Type"  -> "application/json",
                              "Authorization" -> S.authorizationSecret.mkString))
          .onComplete {
            case Success(msg) =>
              val snackText = s"Succesfully persisted state"
              println(snackText)
              showSnackBar(snackText)
            case Failure(AjaxException(xhr)) =>
              val snackText = s"Error persisting state ${xhr.responseType}: ${xhr.responseText}"
              println(snackText)
              showSnackBar(snackText)
            case Failure(err) =>
              val snackText = s"Error persisting state ${err}: ${err.getMessage()}"
              println(snackText)
              showSnackBar(snackText)
          }
        state
      }

      def showDialog(withText: String,
                     changePage: Option[Page] = None,
                     actions: List[Action] = Nil) =
        changePage match {
          case Some(page) =>
            $.modState(
              _.copy(dialogText = withText,
                     dialogOpen = true,
                     currentPage = page,
                     dialogActions = actions))
              .runNow()
          case None =>
            $.modState(_.copy(dialogText = withText, dialogOpen = true, dialogActions = actions))
              .runNow()
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

      def updateWishes(f: List[Wish] => List[Wish]): Unit =
        $.modState(s => {
          val newState = f(s.wishes)
          if (newState == s) {
            s
          } else {
            persist(s.copy(wishes = f(s.wishes)))
          }
        }).runNow()

      val page: ReactElement = S.currentPage match {
        case CreateUser => CreateUserPage(showDialog).build()
        case Login      => LoginPage(handleLogin, $.modState(_.copy(currentPage = CreateUser))).build()
        case Fetching =>
          fetchWishes()
          <.div("Downloading wishes...")
        case WishList =>
          EditWishesPage(
            S.userName.mkString,
            S.wishes,
            S.authorizationSecret.mkString,
            showSnackBar,
            S.editingWishes,
            startEditing,
            stopEditing,
            updateWishes _,
            showDialog
          ).build()
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

      val dialogButtons: List[ReactComponentU_] = S.dialogActions.map {
        case Action(title, callback) =>
          MuiFlatButton(label = title,
                        onClick = (r: ReactEventH) =>
                          $.modState(_.copy(dialogOpen = false))
                            >> callback)()
      } :+ MuiFlatButton(label = "Dismiss",
                         onClick = (r: ReactEventH) => $.modState(_.copy(dialogOpen = false)))()

      val dialog = MuiDialog(
        title = S.dialogText,
        open = S.dialogOpen,
        actions = js.Array(dialogButtons: _*),
        onRequestClose = (b: Boolean) => $.modState(_.copy(dialogOpen = false))
      )()

      MuiMuiThemeProvider(muiTheme = S.theme)(<.div(muiAppBar, page, snackBar, dialog))
    }

  }
}
