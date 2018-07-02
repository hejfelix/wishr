import com.lambdaminute.wishr.model.AuthedApi
import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.web.html.div

import scala.concurrent.Future
import scala.util.{Failure, Success}
import concurrent.ExecutionContext.Implicits.global
import autowire._
import com.lambdaminute.slinkywrappers.materialui.AlignContent.center
import com.lambdaminute.slinkywrappers.materialui._
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`6` => six}
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`12` => twelve}
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`11` => eleven}
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`3` => three}
import com.lambdaminute.slinkywrappers.materialui.Direction._
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`16` => sixteen}
import com.lambdaminute.slinkywrappers.materialui.Modal
import com.lambdaminute.slinkywrappers.materialui.cards.{Card, CardActions, CardContent}
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.model.tags._
import io.circe.generic.auto._
import slinky.core.facade.ReactElement

@react class EditPage extends Component {

  case class State(wishes: List[Wish] = List.empty,
                   owner: Option[String] = None,
                   editingWishId: Int = -1,
                   intentToDeleteWish: Option[Int] = None)

  case class Props(getWishes: () => Future[WishList],
                   getMe: () => Future[UserInfo],
                   navigateToSharedUrl: () => Unit)

  override def initialState: State = State()

  private def deleteWish: () => Unit =
    () =>
      state.intentToDeleteWish.foreach { id =>
        AuthClient[AuthedApi[Future]].deleteWish(id).call().onComplete {
          case Success(_) =>
            this.setState(s =>
              s.copy(wishes = s.wishes.filter(_.id != id), intentToDeleteWish = None))
          case Failure(err) =>
            System.err.println(s"Failed to delete wish ${id}...${err}")
        }
    }

  private def grantWish: () => Unit =
    () =>
      state.intentToDeleteWish.foreach { id =>
        AuthClient[AuthedApi[Future]].grantWish(id).call().onComplete {
          case Success(_) =>
            this.setState(s =>
              s.copy(wishes = s.wishes.filter(_.id != id), intentToDeleteWish = None))
          case Failure(err) =>
            System.err.println(s"Failed to grant wish ${id}...${err}")
        }
    }

  private def modalChoices =
    List(
      ButtonChoice(label = "I got my wish",
                   variant = variant.raised,
                   color = color.primary,
                   action = grantWish),
      ButtonChoice(label = "I no longer want it",
                   variant = variant.raised,
                   color = color.secondary,
                   action = deleteWish),
      ButtonChoice(label = "Cancel",
                   variant = variant.raised,
                   color = color.default,
                   action = () => this.setState(_.copy(intentToDeleteWish = None))),
    )

  def render(): ReactElement =
    div(
      buttons,
      GeneralModal(
        options = modalChoices,
        "Confirm",
        "Please confirm deletion by selecting a reason below. This action cannot be undone",
        open = state.intentToDeleteWish.isDefined
      ),
      Grid(container = true,
           direction = column,
           justify = center,
           spacing = sixteen,
           alignItems = center)(wishCards)
    )

  private val addWish: EventHandler = (_, _) => {
    AuthClient[AuthedApi[Future]].newWish().call().onComplete {
      case Success(newWish) =>
        this.setState(s => s.copy(wishes = newWish :: s.wishes, editingWishId = newWish.id))
      case Failure(err) => System.err.println(err.getMessage)
    }
  }

  private val buttonSize = size.small

  private def addButton: ReactElement =
    Button(onClick = addWish,
           variant = variant.fab,
           color = color.primary,
           size = buttonSize,
           className = "addWishButton")(
      icons.Add()
    )

  private val shareWishList: EventHandler = (_, _) => props.navigateToSharedUrl()

  private def shareWishListButton: ReactElement =
    Button(onClick = shareWishList,
           size = buttonSize,
           variant = variant.fab,
           color = color.secondary)(icons.Share())

  private val buttons =
    Grid(direction = Direction.row, spacing = sixteen, className = "editPageButtons")(
      Grid(item = true)(addButton),
      Grid(item = true)(shareWishListButton))

  private val saveChanges: Wish => Unit = w => {
    AuthClient[AuthedApi[Future]].updateWish(w).call().onComplete {
      case Success(_) =>
        println(s"Saving changes to wish: ${w}")
        this.setState(s =>
          s.copy(editingWishId = -1, wishes = s.wishes.map {
            case Wish(_, _, _, w.id) => w
            case unaffected          => unaffected
          }))
      case Failure(err) =>
        System.err.println(err)
    }

  }

  private val discardChanges: EventHandler = (_, _) => {
    state.wishes
      .filter(_.isEmpty)
      .foreach(w => AuthClient[AuthedApi[Future]].deleteWish(w.id).call())

    state.wishes.find(_.id == state.editingWishId).foreach { w =>
      if (w.isEmpty) {
        println(s"Deleting wish ${w}")
        AuthClient[AuthedApi[Future]].deleteWish(w.id).call.onComplete {
          case Success(_) =>
            println("Non empty: " + state.wishes.filter(!_.isEmpty).mkString("\n"))
            this.setState { s =>
              val nonEmpty = s.wishes.filter(!_.isEmpty)
              println("Setting non empty wishes:" + nonEmpty)
              s.copy(wishes = nonEmpty)
            }
          case Failure(err) =>
            System.err.println(err)
        }
      }
    }
    this.setState(_.copy(editingWishId = -1))
  }

  private def wishCards: List[ReactElement] =
    state.wishes.map { w =>
      val startEditing: EventHandler = (_, _) => this.setState(_.copy(editingWishId = w.id))

      Grid(className = "wishCardItem",
           item = true,
           xs = Layout.mobileCardWidth,
           lg = Layout.desktopCardWidth)
        .withKey(w.heading)(
          card(w, startEditing)
        )
    }

  private def card(w: Wish, startEditing: EventHandler): ReactElement = {
    val onClickDelete: () => Unit = () => this.setState(_.copy(intentToDeleteWish = Option(w.id)))
    WishCard(
      wish = w,
      isEditing = w.id == state.editingWishId,
      startEditing = startEditing,
      saveChanges = saveChanges,
      onClickDelete = onClickDelete,
      discardChanges = discardChanges
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
