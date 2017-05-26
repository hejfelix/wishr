package com.lambdaminute.wishr.component

import cats.syntax.show
import chandu0101.scalajs.react.components.Implicits._
import chandu0101.scalajs.react.components.materialui.{Mui, MuiFlatButton, MuiMuiThemeProvider, _}
import com.lambdaminute.wishr.model.{User, Wish}
import com.lambdaminute.wishr.serialization.OptionPickler.{read, write}
import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
import japgolly.scalajs.react.vdom.prefix_<^.{<, ^, _}
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, TopNode, _}
import org.scalajs.dom.document
import org.scalajs.dom.ext.{Ajax, AjaxException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Failure, Success}
import scala.scalajs.js.timers._
import scalacss.Attrs.color
import scala.concurrent.duration._

import chandu0101.scalajs.react.components.materialui.Mui.SvgIcons.SocialShare

import chandu0101.scalajs.react.components.materialui.Mui.SvgIcons.ActionExitToApp
import chandu0101.scalajs.react.components.materialui.Mui.SvgIcons.ActionBugReport
import chandu0101.scalajs.react.components.materialui.Mui.SvgIcons.ActionCode
import chandu0101.scalajs.react.components.materialui.Mui.SvgIcons.ImageStyle

object WishRAppContainer {

  sealed trait Page
  case object Login      extends Page
  case object Fetching   extends Page
  case object WishList   extends Page
  case object CreateUser extends Page

  sealed trait Theme {
    def name: String
  }
  case object Light extends Theme {
    def name = "Light"
  }
  case object Dark extends Theme {
    def name = "Dark"
  }

  val component: ReactComponentC.ReqProps[Props, State, Backend, TopNode] =
    ReactComponentB[Props]("WishRAppContainer")
      .initialState(State())
      .renderBackend[Backend]
      .componentDidMount { blob =>
        println("Did mount app containerS")
        val p = blob.props
        blob.modState(_.copy(authorizationSecret = p.authorizationSecret, userName = p.userName))
      }
      .build

  def apply(version: String, cookieSecret: Option[String], cookieUser: Option[String]) =
    component(Props(version, authorizationSecret = cookieSecret, userName = cookieUser))

  case class Props(version: String,
                   authorizationSecret: Option[String] = None,
                   userName: Option[String] = None)

  case class Action(title: String, onClick: Callback, level: ActionLevel = Undefined)

  sealed trait ActionLevel
  case object Primary   extends ActionLevel
  case object Secondary extends ActionLevel
  case object Undefined extends ActionLevel

  case class State(currentPage: Page = Login,
                   userName: Option[String] = None,
                   authorizationSecret: Option[String] = None,
                   theme: Theme = Light,
                   errorMessage: Option[String] = None,
                   wishes: List[Wish] = Nil,
                   snackBarText: String = "",
                   snackBarOpen: Boolean = false,
                   dialogText: String = "",
                   dialogOpen: Boolean = false,
                   dialogActions: List[Action] = Nil,
                   editingWishes: List[Wish] = Nil,
                   drawerOpen: Boolean = false)

  class Backend($ : BackendScope[_, State]) {

    def handleLogin(user: Either[String, User]) {
      user match {
        case Left(msg) =>
          $.modState(_.copy(snackBarText = msg, snackBarOpen = true)).runNow()
        case Right(user) =>
          $.modState(
            _.copy(authorizationSecret = Option(user.secret),
                   userName = Option(user.name),
                   currentPage = Fetching)
          ).runNow()
      }
    }

    def render(P: Props, S: State) = {

      println(S)

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
        $.modState(s => {
          val newWishes =
            if (newState.isEmpty) s.wishes.filterNot(_.isEmpty)
            else changeWish(w, newState)(s.wishes)
          persist(s.copy(editingWishes = dropFirstMatch(s.editingWishes, w), wishes = newWishes),
                  showSnackbar = newState.isEmpty)
        }).runNow()

      def persist(state: State, showSnackbar: Boolean = true): State = {
        Ajax
          .post(s"./set",
                write[List[Wish]](state.wishes),
                headers = Map("Content-Type"  -> "application/json",
                              "Authorization" -> S.authorizationSecret.mkString))
          .onComplete {
            case Success(msg) =>
              val snackText = s"Succesfully persisted state"
              println(msg.responseText)
              if (showSnackbar)
                showSnackBar(msg.responseText)
            case Failure(AjaxException(xhr)) =>
              val snackText = s"Error persisting state ${xhr.responseType}: ${xhr.responseText}"
              println(snackText)
              if (showSnackbar)
                showSnackBar(snackText)
            case Failure(err) =>
              val snackText = s"Error persisting state ${err}: ${err.getMessage()}"
              println(snackText)
              if (showSnackbar)
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
        js.timers.setTimeout(1.5.seconds) { //It's a bad experience to flash the loading wheel
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
                $.modState(
                  _.copy(authorizationSecret = None,
                         currentPage = Login,
                         snackBarText = xhr.responseText,
                         snackBarOpen = true)
                ).runNow()
              case Failure(err) =>
                println(err.getMessage)
                $.modState(
                  _.copy(authorizationSecret = None,
                         currentPage = Login,
                         snackBarText = err.getMessage,
                         snackBarOpen = true)
                ).runNow()
            }
        }

      def updateWishes(f: List[Wish] => List[Wish]): Unit =
        $.modState(s => {
          val newState = f(s.wishes)
          if (newState == s) {
            s
          } else {
            val showSnackbar = !newState.headOption.exists(_.isEmpty)
            persist(s.copy(wishes = f(s.wishes)), showSnackbar)
          }
        }).runNow()

      def grantWish(w: Wish) =
        Ajax
          .post("/grant",
                write[Wish](w),
                headers = Map("Content-Type"  -> "application/json",
                              "Authorization" -> S.authorizationSecret.mkString))
          .onComplete {
            case Success(msg) =>
              println(msg.responseText)
              showDialog(msg.responseText)
            case Failure(err) =>
              println(err.getMessage)
              showSnackBar(err.getMessage)
          }

      val page: ReactElement = S.currentPage match {
        case CreateUser => CreateUserPage(showDialog)
        case Login if !(S.userName.isDefined && S.authorizationSecret.isDefined) =>
          LoginPage(handleLogin, $.modState(_.copy(currentPage = CreateUser)))
        case Fetching | Login if S.userName.isDefined && S.authorizationSecret.isDefined =>
          fetchWishes()
          <.div(
            ^.cls := "progress",
            <.h2("Fetching wishes...")(^.cls := "edit-page-title"),
            MuiCircularProgress(mode = DeterminateIndeterminate.indeterminate,
                                size = 256.0,
                                color = Mui.Styles.colors.teal200)()
          )
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
            grantWish,
            showDialog
          )
      }

      val snackBar = MuiSnackbar(
        autoHideDuration = 2500,
        message = S.snackBarText,
        onRequestClose = (x: String) => $.modState(_.copy(snackBarOpen = false)),
        open = S.snackBarOpen
      )()

      val muiAppBar = MuiAppBar(
        title = "WishR",
        showMenuIconButton = true,
        onLeftIconButtonTouchTap = (r: ReactEventH) => $.modState(_.copy(drawerOpen = true))
      )(<.p())

      val dialogButtons: List[ReactComponentU_] = S.dialogActions.map {
        case Action(title, callback, level) =>
          MuiFlatButton(label = title,
                        onClick = (r: ReactEventH) =>
                          $.modState(_.copy(dialogOpen = false))
                            >> callback,
                        primary = level == Primary,
                        secondary = level == Secondary)()
      } :+ MuiFlatButton(label = "Dismiss",
                         onClick = (r: ReactEventH) => $.modState(_.copy(dialogOpen = false)))()

      val title = <.div(^.key := "title", <.h1("WishR"))
      val version = MuiMenuItem(
        key = "versionitem",
        primaryText = s"Version ${P.version}"
      )()
      val logout = MuiMenuItem(
        key = "logout",
        primaryText = "Logout",
        leftIcon = ActionExitToApp()(),
        onTouchTap = (r: ReactEventH) => {
          println("Resetting cookie...")
          org.scalajs.dom.document.cookie = "authsecret=;Max-Age=0"
          org.scalajs.dom.document.cookie = "authname=;Max-Age=0"
          $.modState(_.copy(authorizationSecret = None, userName = None, currentPage = Login))
        }
      )()

      def showSharingLink =
        Ajax
          .get("/sharingURL",
               headers = Map("Content-Type"  -> "application/json",
                             "Authorization" -> S.authorizationSecret.mkString))
          .onComplete {
            case Success(msg) =>
              showDialog(msg.responseText)
            case Failure(err) =>
              showSnackBar(err.getMessage)
          }

      val getLinkForSharing = MuiMenuItem(
        key = "getSharingLink",
        primaryText = "Get link for sharing",
        rightIcon = SocialShare()(),
        onTouchTap = (r: ReactEventH) => Callback(showSharingLink)
      )()

      def toggleTheme(s: State) =
        s.theme match {
          case Light => s.copy(theme = Dark)
          case Dark  => s.copy(theme = Light)
        }

      val toggleThemeMenuItem = MuiMenuItem(
        key = "ToggleTheme",
        rightIcon = ImageStyle()(),
        primaryText = s"${S.theme.name} Theme",
        onTouchTap = (r: ReactEventH) => $.modState(toggleTheme)
      )()

      def toggleDrawerOpen =
        (b: Boolean, s: String) =>
          Callback.info(s"toggle drawer $b $s") >> $.modState(s =>
            s.copy(drawerOpen = !s.drawerOpen))

      val fileBugs = MuiMenuItem(
        key = "fileBugsMenuItem",
        primaryText = "Submit Bug Reports",
        rightIcon = ActionBugReport()(),
        onTouchTap = (r: ReactEventH) =>
          Callback(
            org.scalajs.dom.window
              .open(url = "https://github.com/hejfelix/wishr/issues", target = "_blank")
        )
      )()

      val contribute = MuiMenuItem(
        key = "contributeMenuItem",
        primaryText = "Contribute",
        rightIcon = ActionCode()(),
        onTouchTap = (r: ReactEventH) =>
          Callback(
            org.scalajs.dom.window
              .open(url = "https://github.com/hejfelix/wishr/", target = "_blank")
        )
      )()

      val loggedIn = S.userName.isDefined && S.authorizationSecret.isDefined
      val drawer =
        MuiDrawer(open = S.drawerOpen, docked = false, onRequestChange = toggleDrawerOpen)(
          title,
          version,
          if (loggedIn) logout else null,
          if (loggedIn) getLinkForSharing else null,
          toggleThemeMenuItem,
          fileBugs,
          contribute)

      val dialog = MuiDialog(
        title = S.dialogText,
        open = S.dialogOpen,
        actions = js.Array(dialogButtons: _*),
        onRequestClose = (b: Boolean) => $.modState(_.copy(dialogOpen = false))
      )()

      def withBaseColor(theme: MuiRawTheme)(baseColor: MuiColor) =
        theme.copy(palette = theme.palette.copy(primary1Color = baseColor))

      val theme = S.theme match {
        case Light =>
          document.body.style.backgroundColor = "#b7e9ff"
          val theme = Mui.Styles.LightRawTheme
          Mui.Styles.getMuiTheme(withBaseColor(theme)(Mui.Styles.colors.lightBlue200))
        case Dark =>
          document.body.style.backgroundColor = "#434343"
          val darkRawTheme = Mui.Styles.DarkRawTheme
          Mui.Styles.getMuiTheme(withBaseColor(darkRawTheme)(Mui.Styles.colors.grey500))
      }

      MuiMuiThemeProvider(muiTheme = theme)(
        <.div(
          muiAppBar,
          ReactCssTransitionGroup(
            "wish",
            component = "div",
            enterTimeout = 300,
            leaveTimeout = 300)(<.div(^.key := S.currentPage.toString, ^.cls := "page", page)),
          snackBar,
          dialog,
          drawer
        ))
    }

  }
}
