import com.lambdaminute.slinkywrappers.reactrouter.RouteProps
import com.lambdaminute.wishr.model.{AuthedApi, UnauthedApi, UserInfo, Wish}
import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.core.facade.{Fragment, ReactElement}
import slinky.web.html.div
import autowire._
import com.lambdaminute.slinkywrappers.materialui.AlignContent.center
import com.lambdaminute.slinkywrappers.materialui.Direction.column
import com.lambdaminute.slinkywrappers.materialui._
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`16` => sixteen}
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.scalajs.js.JSON
import concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
@react class SharedPage extends Component {

  case class State(wishes: List[Wish] = Nil, sharerInfo: Option[UserInfo] = None)
  case class Props(secretToken: String)

  override def initialState = State()

  override def componentWillMount(): Unit =
    Client[UnauthedApi].getSharedWishes(props.secretToken).call().onComplete {
      case Success((wishes, userInfo)) =>
        this.setState(_.copy(wishes = wishes, sharerInfo = Option(userInfo)))
      case Failure(err) =>
        System.err.println(err.getMessage)
    }

  val nop: EventHandler = (_, _) => ()

  override def render(): ReactElement =
    div(
      Grid(container = true,
           direction = column,
           justify = center,
           spacing = sixteen,
           alignItems = center)(
        Typography(align = align.center, variant = textvariant.display1)(
          state.sharerInfo.map(info =>
            s"${info.firstName} ${info.lastName} (${info.email}) shared this wish list with you")),
        state.wishes.map(
          w =>
            Grid(item = true,
                 xs = Layout.mobileCardWidth,
                 lg = Layout.desktopCardWidth,
                 className = "wishCardItem")
              .withKey(w.heading)(card(w)))
      )
    )

  private def card(w: Wish): ReactElement =
    WishCard(
      wish = w,
      isEditing = false,
      startEditing = nop,
      saveChanges = (_: Wish) => (),
      onClickDelete = () => (),
      discardChanges = nop,
      noButtons = true
    )
}
