import com.lambdaminute.slinkywrappers.materialui.cards.{Card, CardActions, CardContent}
import com.lambdaminute.slinkywrappers.materialui._
import slinky.core.StatelessComponent
import slinky.core.facade.ReactElement
import slinky.core.annotations.react

case class ButtonChoice(label: String, variant: variant, color: color, action: () => Unit)

@react class GeneralModal extends StatelessComponent {

  case class Props(
      options: List[ButtonChoice],
      title: String,
      description: String,
      open: Boolean
  )

  def buttons: List[ReactElement] = props.options.map {
    case ButtonChoice(label, variant, color, action) =>
      val onClick: EventHandler = (_, _) => action()
      Button(variant = variant, color = color, onClick = onClick)(label)
  }

  override def render(): ReactElement =
    Modal(open = props.open)(
      Card(raised = true, className = "modal")(
        CardContent(
          Typography(variant = textvariant.headline)(props.title),
          Typography(variant = textvariant.body1)(props.description),
          CardActions(
            buttons
          )
        )))
}
