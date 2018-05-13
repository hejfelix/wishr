import com.lambdaminute.slinkywrappers.reactrouter.RouteProps
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.web.html.{div, h1}

@react class EditPage extends StatelessComponent {
  type Props = Unit
  def render() = div(h1("Edit Page"))
}
