import com.lambdaminute.slinkywrappers.materialui.Direction.{column, row}
import com.lambdaminute.slinkywrappers.materialui.Sizes.{
  `0` => zero,
  `12` => twelve,
  `2` => two,
  `3` => three,
  `5` => five,
  `6` => six,
  `7` => seven
}
import com.lambdaminute.slinkywrappers.materialui._
import com.lambdaminute.slinkywrappers.materialui.size
import com.lambdaminute.slinkywrappers.materialui.Divider
import com.lambdaminute.slinkywrappers.materialui.cards._
import com.lambdaminute.slinkywrappers.materialui.align.justify
import com.lambdaminute.slinkywrappers.materialui.AlignContent.stretch
import com.lambdaminute.slinkywrappers.materialui.AlignContent.center
import com.lambdaminute.slinkywrappers.materialui.AlignContent.{`space-between` => spaceBetween}
import com.lambdaminute.wishr.model.Wish
import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.core.facade.{Fragment, ReactElement}
import slinky.web.html._
import slinky.web.svg.direction
import AlignContent.{`space-between` => spaceBetween}
import org.scalajs.dom.Event
import org.scalajs.dom.raw.{HTMLFormElement, HTMLInputElement}

import scala.scalajs.js
import scala.scalajs.js.URIUtils.encodeURI
import scala.scalajs.js.{JSON, URIUtils}

@react class WishCard extends Component {

  case class State(imageUrl: String)
  case class Props(wish: Wish,
                   isEditing: Boolean,
                   startEditing: EventHandler,
                   saveChanges: Wish => Unit,
                   onClickDelete: () => Unit,
                   discardChanges: EventHandler,
                   noButtons: Boolean = false)

  override def initialState: State = State(props.wish.image.mkString)

  private val buttonSize = size.small

  private val allPartDesktop = twelve
  private val allPartMobile  = twelve

  private val mobileImageSize  = five
  private val desktopImageSize = five

  private val mobileTextSize  = seven
  private val desktopTextSize = seven

  override def render(): ReactElement =
    if (props.isEditing) form(onSubmit := handleSubmit)(card) else card

  private def editingCardActions: ReactElement =
    Grid(item = true)(
      Button(color = color.primary, variant = variant.raised, size = buttonSize)(
        `type` := "submit")("Save changes"),
      Button(color = color.secondary, onClick = props.discardChanges, size = buttonSize)(
        "Discard changes")
    )

  private def onClickOpen(url: String): EventHandler =
    (_, _) => org.scalajs.dom.window.open(encodeURI(url), "_blank")

  private val searchButtonVariant = variant.fab
  private val searchButtonSize    = size.small
  private def searchButtons = Grid(container = true, direction = row)(
    IconButton(
      onClick = onClickOpen(s"http://www.pricerunner.dk/search?q=${encodeURI(props.wish.heading)}"),
      color = color.default
    )(img(src := "pricerunner.svg", className := "searchButton")),
    IconButton(
      onClick =
        onClickOpen(s"https://www.amazon.co.uk/s/?field-keywords=${encodeURI(props.wish.heading)}"),
      color = color.default
    )(img(src := "amazon.svg", className := "searchButton")),
    IconButton(onClick =
                 onClickOpen(s"https://www.google.dk/#q=${encodeURI(props.wish.heading)}&tbm=shop"),
               color = color.default)(img(src := "google.svg", className := "searchButton"))
  )

  private def onClickDelete: EventHandler = (_, _) => props.onClickDelete()

  private def actions =
    Grid(container = true, justify = spaceBetween, direction = Direction.row, alignItems = center)(
      Grid(item = true)(
        if (props.noButtons) Fragment()
        else if (props.isEditing) editingCardActions
        else viewingCardActions),
      Grid(item = true)(searchButtons)
    )

  private def viewingCardActions: ReactElement =
    Grid(container = true, direction = row, spacing = zero)(
      Grid(item = true)(
        Button(onClick = props.startEditing,
               variant = variant.raised,
               color = color.primary,
               className = "editButtons",
               size = buttonSize)(icons.Edit())),
      Grid(item = true)(
        Button(onClick = onClickDelete,
               variant = variant.raised,
               color = color.default,
               className = "editButtons",
               size = buttonSize)(icons.Delete()))
    )

  private val handleSubmit: js.Function1[Event, Unit] = { event =>
    event.preventDefault()

    val elements = event.target.asInstanceOf[HTMLFormElement].elements
    val title =
      elements.namedItem(titleFieldName).asInstanceOf[HTMLInputElement].value
    val description =
      elements.namedItem(descriptionFieldName).asInstanceOf[HTMLInputElement].value
    val image = elements.namedItem(imageFieldName).asInstanceOf[HTMLInputElement].value
    println(event.target)
    println(title)
    println(description)
    println(image)
    props.saveChanges(Wish(title, description, Option(image), props.wish.id))
  }

  private val titleFieldName       = "titleField"
  private val descriptionFieldName = "descriptionField"
  private val imageFieldName       = "imageField"

  private def card: ReactElement =
    Card(className = "wishCardContainer")(
      div(style := js.Dynamic.literal(width = "100%"))(
        Grid(container = true,
             direction = row,
             alignItems = stretch /*, xs = allPartMobile, lg = allPartDesktop*/ )(
          Grid(item = true, xs = mobileTextSize, lg = desktopTextSize)(
            if (props.isEditing) editingWishText else wishText),
          image,
        ),
        Divider(),
        actions
      )
    )

  private def onUrlChange: js.Function1[js.Object, Unit] = { (event) =>
    val inputValue = event.asInstanceOf[Event].target.asInstanceOf[HTMLInputElement].value
    println(inputValue)
    this.setState(_.copy(imageUrl = inputValue))
  }

  private def image =
    Grid(item = true, xs = mobileImageSize, lg = desktopImageSize)(
      Grid(container = true, className = "imageGridItem", direction = column)(
        if (props.isEditing)
          Grid(item = true)(
            TextField(label = "Image URL",
                      name = imageFieldName,
                      defaultValue = props.wish.image.mkString,
                      onChange = onUrlChange))
        else
          Grid(item = true)(),
        Grid(item = true)(imageElement)
      )
    )

  def imageElement: ReactElement =
    if (state.imageUrl.nonEmpty) img(src := state.imageUrl, className := "wishCardImg")
    else
      img(src := "https://imgplaceholder.com/600x400/eeeeee/333333/fa-image",
          className := "wishCardImg")

  private def wishText: ReactElement =
    Grid(container = true,
         direction = column,
         justify = spaceBetween,
         className = "wishTextContainer")(
      Grid(item = true)(Typography(variant = textvariant.title)(props.wish.heading),
                        Typography(props.wish.desc))
    )

  private def editingWishText: ReactElement =
    Grid(item = true, xs = mobileTextSize, lg = desktopTextSize)(
      Grid(container = true, direction = column, justify = spaceBetween)(
        Grid(item = true)(
          TextField(label = "Title",
                    autoFocus = true,
                    name = titleFieldName,
                    defaultValue = props.wish.heading),
          br(),
          TextField(label = "Description",
                    name = descriptionFieldName,
                    multiline = true,
                    defaultValue = props.wish.desc),
        )
      )
    )
}
