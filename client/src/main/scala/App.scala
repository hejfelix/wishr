import com.lambdaminute.slinkywrappers.materialui.MaterialUi.colors._
import com.lambdaminute.slinkywrappers.materialui.color.primary
import com.lambdaminute.slinkywrappers.materialui.{Typography, icons, _}
import com.lambdaminute.slinkywrappers.materialui.position._
import com.lambdaminute.slinkywrappers.reactrouter._
import com.lambdaminute.wishr.model.AuthedApi
import com.lambdaminute.wishr.model.tags.SessionToken
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.annotation.JSImport
import autowire._
import com.lambdaminute.wishr.model.tags._
import com.lambdaminute.wishr.model._
import io.circe.generic.auto._
import concurrent.ExecutionContext.Implicits.global

@js.native
@JSImport("@material-ui/icons/AccessAlarm", JSImport.Default)
object AccessAlarmIcon extends js.Object

@react class App extends Component {
  type Props = RouteProps

  case class State(drawerOpen: Boolean = false, token: Option[SessionToken] = None)

  override def initialState: State = State()

  val loginPath      = "/login"
  val editWishesPath = "/edit"

  private val themeSettings = literal {
    palette = literal {
      primary = indigo
    }
  }

  private val daftTheme: Theme = MaterialUi.createMuiTheme(themeSettings)

  val getWishes: () => Future[WishList] = () => AuthClient[AuthedApi[Future]].getWishes().call()
  val getMe: () => Future[UserInfo]     = () => AuthClient[AuthedApi[Future]].me().call()

  def render(): ReactElement = {
    println(s"RENDER: ${props.location.pathname}")
    MuiThemeProvider(theme = daftTheme)(
      AppBar(position = static, color = color.primary)(
        Toolbar(
          IconButton(color = color.inherit)(
            icons.Menu()
          ),
          Typography(variant = textvariant.title, color = textcolor.inherit)(
            props.location.pathname.toString.drop(1).mkString)
        )
      ),
      div(
        ul(
          li(Link(to = loginPath)("Login")),
          li(Link(to = editWishesPath)("Edit"))
        ),
        Route(exact = true, path = loginPath, render = (_: RouteProps) => LoginPage()),
        Route(exact = true,
              path = editWishesPath,
              render = (_: RouteProps) => EditPage(getMe = getMe, getWishes = getWishes))
      )
    )
  }
}
