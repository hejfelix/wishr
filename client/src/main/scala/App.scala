import autowire._
import com.lambdaminute.slinkywrappers.materialui.MaterialUi.colors._
import com.lambdaminute.slinkywrappers.materialui.color.primary
import com.lambdaminute.slinkywrappers.materialui.position._
import com.lambdaminute.slinkywrappers.materialui.{Typography, icons, _}
import com.lambdaminute.slinkywrappers.reactrouter._
import com.lambdaminute.wishr.model.{AuthedApi, _}
import org.scalajs.dom.ext.AjaxException
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.{Fragment, ReactElement}
import slinky.web.html._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.annotation.JSImport

import io.circe.generic.auto._

@js.native
@JSImport("@material-ui/icons/AccessAlarm", JSImport.Default)
object AccessAlarmIcon extends js.Object

object AppRoutes {
  val loginPath      = "/login"
  val editWishesPath = "/edit"
}

@react class App extends Component {
  type Props = RouteProps

  case class State(drawerOpen: Boolean = false,
                   errorMessage: Option[String] = None,
                   userInfo: Option[UserInfo] = None,
                   gravatarUrl: String = "",
                   loggedIn: Boolean = false)

  override def initialState: State = State()

  private val themeSettings = literal {
    palette = literal {
      primary = indigo
    }
  }

  private val setAsLoggedIn: js.Function = () => this.setState(_.copy(loggedIn = true))

  private val daftTheme: Theme = MaterialUi.createMuiTheme(themeSettings)

  private val logOut = () => {
    AuthClient.logOut
    this.setState(_.copy(userInfo = None, gravatarUrl = ""))
  }

  val getWishes: () => Future[WishList] = () => AuthClient[AuthedApi[Future]].getWishes().call()
  val getMe: () => Future[UserInfo]     = () => AuthClient[AuthedApi[Future]].me().call()
  val getGravatarUrl: () => Future[String] = () =>
    AuthClient[AuthedApi[Future]].gravatarUrl().call()

  val navigateToSharedUrl: () => Unit = () =>
    this.state.userInfo.foreach { info =>
      this.props.history.push.asInstanceOf[js.Function1[String, Unit]](
        s"/?sharedURL=${info.secretUrl}"
      )
  }

  private def defaultPath: Breakpoint =
    if (AuthClient.isLoggedIn) AppRoutes.editWishesPath else AppRoutes.loginPath

  def parseSecretToken(search: String): Option[String] = {
    val (key, value) = search.drop(1).span(_ != '=')
    println(s"Parse: ${key} ${value}")
    if (key == "sharedURL") Option(value.drop(1).mkString) else None
  }

  private val updateUserInfo: () => Unit = () =>
    if (AuthClient.isLoggedIn) {
      for {
        info        <- getMe()
        gravatarUrl <- getGravatarUrl()
      } {
        this.setState(s => s.copy(userInfo = Option(info), gravatarUrl = gravatarUrl))
      }
  }

  override def componentDidMount(): Unit = {
    super.componentDidMount()
    def onError(ajaxException: AjaxException) = {
      println(s"Setting error text: ${ajaxException.getMessage}")
      val errorText = s"ERROR ${ajaxException.xhr.status}: ${ajaxException.xhr.responseText}"
      this.setState(_.copy(errorMessage = Option(errorText)))
    }
    AuthClient.setErrorCallback(onError _)
    Client.setErrorCallback(onError _)
    updateUserInfo()
  }

  private val setUserInfo = (info: UserInfo) => this.setState(_.copy(userInfo = Option(info)))

  private val onMenuClick: EventHandler = (_, _) =>
    this.setState(s => s.copy(drawerOpen = !s.drawerOpen))

  private val onDrawerClose: () => Unit = () => this.setState(_.copy(drawerOpen = false))

  def render(): ReactElement =
    MuiThemeProvider(theme = daftTheme)(
      DrawerMenu(state.drawerOpen, logOut, onDrawerClose, state.userInfo.isDefined),
      div(
        AppBar(position = fixed, color = color.primary)(
          Toolbar(
            IconButton(color = color.inherit, onClick = onMenuClick)(
              icons.Menu()
            ),
            Typography(
              variant = textvariant.title,
              color = textcolor.inherit,
              className = "appBarTitle")(props.location.pathname.toString.drop(1).mkString),
            Avatar(src = state.gravatarUrl)
          )
        )),
      div(style := js.Dynamic.literal(paddingTop = "5em"))(
        state.errorMessage.fold(Fragment())(errMsg => Typography(color = textcolor.error)(errMsg)),
        Route(
          exact = true,
          path = AppRoutes.loginPath,
          render = (rp: RouteProps) =>
            LoginPage(updateUserInfo = this.updateUserInfo,
                      push = this.props.history.push.asInstanceOf[js.Function1[String, Unit]])
        ),
        Route(exact = true,
              path = AppRoutes.editWishesPath,
              render = (_: RouteProps) =>
                EditPage(getMe = getMe,
                         getWishes = getWishes,
                         navigateToSharedUrl = navigateToSharedUrl)),
        Route(path = "/", render = rp => sharedPageOrDefault(rp))
      )
    )

  private def sharedPageOrDefault(rp: RouteProps): ReactElement = {
    println(s"ROUTE PROPS: ${rp.location.search}")

    parseSecretToken(rp.location.search.toString)
      .fold[ReactElement](Redirect(to = defaultPath, from = "/"))(tkn => SharedPage(tkn))
  }
}
