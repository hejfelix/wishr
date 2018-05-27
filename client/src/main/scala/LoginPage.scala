import com.lambdaminute.slinkywrappers.materialui.AlignContent.center
import com.lambdaminute.slinkywrappers.materialui._
import com.lambdaminute.slinkywrappers.materialui.align.justify
import com.lambdaminute.slinkywrappers.materialui.cards._
import org.scalajs.dom.Event
import org.scalajs.dom.raw.{HTMLFormElement, HTMLInputElement}
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.scalajs.js

@react class LoginPage extends StatelessComponent {
  type Props = Unit
  private val emailInputName    = "email"
  private val passwordInputName = "password"

  override def componentDidMount(): Unit = {
    super.componentDidMount()
    println("LOGINPAGE")
  }

  def render(): ReactElement =
    div(
      Grid(container = true, justify = center, alignItems = center)(
        Grid(item = true)(
          Card(raised = true, className = "card")(
            Grid(container = true, justify = center, alignItems = center)(cardContent))
        ))
    )

  private val handleSubmit: js.Function1[Event, Unit] = e => {
    e.preventDefault()
    val elements = e.target.asInstanceOf[HTMLFormElement].elements
    val password =
      elements.namedItem(passwordInputName).asInstanceOf[HTMLInputElement].value
    val email =
      elements.namedItem(emailInputName).asInstanceOf[HTMLInputElement].value
    println(s"Submitting state: ${email}, ${password}")
  }

  private def cardContent =
    CardContent(
      form(onSubmit := handleSubmit)(
        TextField(label = "E-mail",
                  placeholder = "e-mail",
                  className = "textField",
                  name = emailInputName),
        br(),
        TextField(label = "Password",
                  placeholder = "password",
                  `type` = "password",
                  className = "textField",
                  name = passwordInputName),
        br(),
        Button(variant = variant.raised, color = color.primary, className = "loginButton")(
          `type` := "submit")("Log in")
      )
    )

}
