import com.lambdaminute.slinkywrappers.materialui._
import com.lambdaminute.slinkywrappers.materialui.cards._
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`7` => textPart}
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`5` => imagePart}
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`12` => allPart}
import com.lambdaminute.slinkywrappers.materialui.Direction.row
import com.lambdaminute.wishr.model.Wish
import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react class WishCard extends Component {

  case class State(isEditing: Boolean = true)
  case class Props(wish: Wish)

  override def initialState: State = State()

  override def render(): ReactElement = card

  private def saveChanges: EventHandler    = (_, _) => this.setState(_.copy(isEditing = false))
  private def discardChanges: EventHandler = (_, _) => this.setState(_.copy(isEditing = false))
  private def startEditing: EventHandler   = (_, _) => this.setState(_.copy(isEditing = true))

  private def editingCardActions =
    CardActions(
      Button(color = color.primary, variant = variant.raised, onClick = saveChanges)(
        "Save changes"),
      Button(color = color.secondary, onClick = discardChanges)("Discard changes")
    )

  private def viewingCardActions =
    Button(onClick = startEditing, variant = variant.fab, color = color.primary)(
      icons.Edit()
    )

  private def card =
    Card(className = "wishCardContainer")(
      Grid(container = true, direction = row, xs = allPart)(
        if (state.isEditing) editingWishText else wishText,
        Grid(item = true, xs = imagePart, className = "imageGridItem")(
          img(src := props.wish.image.mkString, className := "wishCardImg")),
        CardActions(if (state.isEditing) editingCardActions else viewingCardActions)
      ),
    )

  private def wishText =
    Grid(item = true, xs = textPart)(
      Typography(variant = textvariant.title)(props.wish.heading),
      Typography(props.wish.desc)
    )

  private def editingWishText =
    Grid(item = true, xs = textPart)(
      TextField(label = "Title", defaultValue = props.wish.heading),
      br(),
      TextField(label = "Description", multiline = true, defaultValue = props.wish.desc)
    )
}
