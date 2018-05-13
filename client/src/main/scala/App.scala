import com.lambdaminute.slinkywrappers.materialui.color.primary
import com.lambdaminute.slinkywrappers.materialui.position._
import com.lambdaminute.slinkywrappers.materialui._
import com.lambdaminute.slinkywrappers.materialui.icons
import com.lambdaminute.slinkywrappers.reactrouter._
import slinky.core._
import slinky.core.annotations.react
import slinky.web.html._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@material-ui/icons/AccessAlarm", JSImport.Default)
object AccessAlarmIcon extends js.Object
@react class App extends StatelessComponent {
  type Props = Unit

  val loginPath      = "/login"
  val editWishesPath = "/edit"

  private val themeSettings = literal {
    palette = literal {
      primary = MaterialUi.colors.blue
    }
  }

  private val daftTheme: Theme = MaterialUi.createMuiTheme(themeSettings)

  def render() =
    MuiThemeProvider(theme = daftTheme)(
      AppBar(position = static, color = color.primary)(
        Toolbar(
          IconButton(color = color.inherit)(
            icons.Menu()
          )
        )
      ),
      BrowserRouter(
        div(
          ul(
            li(Link(to = loginPath)("Login")),
            li(Link(to = editWishesPath)("Edit"))
          ),
          Route(exact = true, path = loginPath, render = (_: RouteProps) => LoginPage()),
          Route(exact = true, path = editWishesPath, render = (_: RouteProps) => EditPage())
        )
      )
    )
}
