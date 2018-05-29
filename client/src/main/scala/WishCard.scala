import com.lambdaminute.slinkywrappers.materialui.Typography
import com.lambdaminute.slinkywrappers.materialui.cards.{CardMedia, _}
import com.lambdaminute.slinkywrappers.materialui.textvariant
import com.lambdaminute.wishr.model.Wish
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react class WishCard extends StatelessComponent {
  case class Props(wish: Wish)

  override def render(): ReactElement = Card(className = "wishCard")(
    div(className := "cardDiv")(
      CardContent(
        Typography(variant = textvariant.headline)(props.wish.heading),
        Typography(component = "p")(props.wish.desc)
      )
    ),
      CardMedia (image = props.wish.image.mkString, className = "wishCardMedia"),
  )

}
