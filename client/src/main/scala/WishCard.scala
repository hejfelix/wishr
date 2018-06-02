import com.lambdaminute.slinkywrappers.materialui.Direction.{column, row}
import com.lambdaminute.slinkywrappers.materialui.Sizes.{
  `12` => allPart,
  `5` => imagePart,
  `7` => textPart
}
import com.lambdaminute.slinkywrappers.materialui._
import com.lambdaminute.slinkywrappers.materialui.align.justify
import com.lambdaminute.slinkywrappers.materialui.cards._
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
import scala.scalajs.js.JSON

@react class WishCard extends StatelessComponent {

  case class Props(wish: Wish,
                   isEditing: Boolean,
                   startEditing: EventHandler,
                   saveChanges: Wish => Unit,
                   discardChanges: EventHandler)

  override def render(): ReactElement =
    if (props.isEditing) form(onSubmit := handleSubmit)(card) else card

  private def editingCardActions: ReactElement =
    Fragment(
      Button(color = color.primary, variant = variant.raised)(`type` := "submit")("Save changes"),
      Button(color = color.secondary, onClick = props.discardChanges)("Discard changes")
    )

  private def viewingCardActions: ReactElement =
    Button(onClick = props.startEditing, variant = variant.raised, color = color.primary)(
      icons.Edit()
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
      Grid(container = true, direction = row, xs = allPart)(
        if (props.isEditing) editingWishText else wishText,
        image,
      ),
    )

  private def image =
    Grid(container = true, xs = imagePart, className = "imageGridItem", direction = column)(
      if (props.isEditing)
        Grid(item = true)(
          TextField(label = "Image URL",
                    name = imageFieldName,
                    defaultValue = props.wish.image.mkString))
      else
        Grid(item = true)(),
      Grid(item = true)(img(src := props.wish.image.mkString, className := "wishCardImg"))
    )

  private def wishText: ReactElement =
    Grid(container = true, direction = column, justify = spaceBetween, xs = textPart)(
      Grid(item = true)(Typography(variant = textvariant.title)(props.wish.heading),
                        Typography(props.wish.desc)),
      Grid(item = true)(viewingCardActions)
    )

  private def editingWishText: ReactElement =
    Grid(container = true, direction = column, justify = spaceBetween, xs = textPart)(
      Grid(item = true)(
        TextField(label = "Title",autoFocus = true, name = titleFieldName, defaultValue = props.wish.heading),
        br(),
        TextField(label = "Description",
                  name = descriptionFieldName,
                  multiline = true,
                  defaultValue = props.wish.desc),
      ),
      Grid(item = true)(editingCardActions)
    )
}
