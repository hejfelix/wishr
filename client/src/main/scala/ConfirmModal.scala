import com.lambdaminute.slinkywrappers.materialui.cards.{Card, CardActions, CardContent}
import com.lambdaminute.slinkywrappers.materialui._
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement

@react class ConfirmModal extends StatelessComponent {

  case class Props(onYes: () => Unit,
                   onNo: () => Unit,
                   title: String,
                   description: String,
                   open: Boolean)

  private def onYes: EventHandler = (_, _) => props.onYes()
  private def onNo: EventHandler  = (_, _) => props.onNo()

  override def render(): ReactElement =
    Modal(open = props.open)(
      Card(raised = true, className = "modal")(CardContent(
        Typography(variant = textvariant.headline)(props.title),
        Typography(variant = textvariant.body1)(props.description),
        CardActions(
          Button(variant = variant.raised, color = color.primary, onClick = onYes)("OK"),
          Button(variant = variant.flat, color = color.secondary, onClick = onNo)("Cancel")
        )
      )))

}
