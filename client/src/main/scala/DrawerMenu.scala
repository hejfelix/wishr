import com.lambdaminute.slinkywrappers.materialui.{ListItem, _}
import org.scalajs.dom.Event
import slinky.core.{BuildingComponent, StatelessComponent}
import slinky.core.annotations.react
import slinky.core.facade.{Fragment, ReactElement}
import slinky.web.html._

import scala.scalajs.js

@react class DrawerMenu extends StatelessComponent {
  case class Props(open: Boolean, logOut: () => Unit, onDrawerClose: () => Unit, loggedIn: Boolean)

  val doClick: js.Function1[Event, Unit] = _ => props.logOut()
  val onClose: js.Function               = () => props.onDrawerClose()

  private def logOutItem: ReactElement =
    ListItem(button = true)(onClick := doClick)(icons.ExitToApp(),
                                                ListItemText(primary = ("Logout": ReactElement)))

  private def submitBugReportItem: ReactElement =
    ListItem(button = true)(icons.BugReport(),
                            ListItemText(primary = ("Submit bug report": ReactElement)))

  private def menuHeader: ReactElement =
    ListSubheader("Actions")

  override def render(): ReactElement =
    Drawer(open = props.open, anchor = Anchor.left, onClose = onClose)(
      MList(
        menuHeader,
        if (props.loggedIn) logOutItem else Fragment(),
        submitBugReportItem
      )
    )

}
