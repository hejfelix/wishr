import com.lambdaminute.wishr.model.AuthedApi
import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.web.html.div

import scala.concurrent.Future
import scala.util.{Failure, Success}
import concurrent.ExecutionContext.Implicits.global
import autowire._
import com.lambdaminute.slinkywrappers.materialui.AlignContent.center
import com.lambdaminute.slinkywrappers.materialui.{EventHandler, Grid}
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`12` => twelve}
import com.lambdaminute.slinkywrappers.materialui.Direction._
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`16` => sixteen}
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.model.tags._
import io.circe.generic.auto._
import slinky.core.facade.ReactElement

@react class EditPage extends Component {

  case class State(wishes: List[Wish] = List.empty,
                   owner: Option[String] = None,
                   editingWishId: Int = -1)
  case class Props(getWishes: () => Future[WishList], getMe: () => Future[UserInfo])

  override def initialState: State = State()

  def render(): ReactElement =
    div(
      Grid(container = true,
           direction = column,
           justify = center,
           spacing = sixteen,
           alignItems = center)(wishCards))

  private def wishCards: List[ReactElement] =
    state.wishes.map { w =>
      val startEditing: EventHandler = (_, _) => this.setState(_.copy(editingWishId = w.id))
      val saveChanges: Wish => Unit = w => {
        println(s"Saving changes to wish: ${w}")
        this.setState(s =>
          s.copy(editingWishId = -1, wishes = s.wishes.map {
            case Wish(_, _, _, w.id) => w
            case unaffected          => unaffected
          }))
      }
      val discardChanges: EventHandler = (_, _) => this.setState(_.copy(editingWishId = -1))

      Grid(xs = twelve, item = true).withKey(w.heading)(
        WishCard(wish = w,
                 isEditing = w.id == state.editingWishId,
                 startEditing = startEditing,
                 saveChanges = saveChanges,
                 discardChanges = discardChanges)
      )
    }

  override def componentDidMount(): Unit = {
    super.componentDidMount()
    props.getWishes().onComplete {
      case Success(wishList) =>
        this.setState(_.copy(wishes = wishList.wishes, owner = Option(wishList.owner)))
      case Failure(err) =>
        println(s"Failed to get wishes: ${err.getMessage}")
    }
  }

}
