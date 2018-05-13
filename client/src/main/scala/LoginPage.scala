import com.lambdaminute.slinkywrappers.reactrouter.RouteProps
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{div, h1}

@react class LoginPage extends StatelessComponent {
  type Props = Unit

  override def componentDidMount(): Unit = {
    super.componentDidMount()
    println("LOGINPAGE")
  }

  def render():ReactElement = div(h1("Login"))

}
