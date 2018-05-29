import com.lambdaminute.slinkywrappers.materialui.{Anchor, Drawer}
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement

@react class DrawerMenu extends StatelessComponent {
  case class Props(open: Boolean)

  override def render(): ReactElement = Drawer(open = props.open, anchor = Anchor.left)

}
