import com.lambdaminute.slinkywrappers.materialui.AlignContent.center
import com.lambdaminute.slinkywrappers.materialui._
import com.lambdaminute.slinkywrappers.materialui.align.justify
import com.lambdaminute.slinkywrappers.materialui.cards._
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.model.tags._
import org.scalajs.dom.Event
import org.scalajs.dom.raw.{HTMLFormElement, HTMLInputElement}
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import autowire._
import com.lambdaminute.slinkywrappers.reactrouter.Redirect

import scala.concurrent.Future
import scala.scalajs.js
import concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@react class LoginPage extends StatelessComponent {
  case class Props(push: js.Function1[String, Unit])
  private val emailInputName    = "email"
  private val passwordInputName = "password"

  override def componentDidMount(): Unit = {
    super.componentDidMount()
    println("LOGINPAGE")
  }

  def render(): ReactElement =
    div(
      Grid(container = true, justify = center, alignItems = center)(
        Grid(item = true, xs = true)(
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

    Client[UnauthedApi].logIn(email.asEmail, password.asPassword).call().onComplete {
      case Success(value) =>
        println(s"Successfully logged in ${value}")
        AuthClient.setToken(value.sessionToken)

        val location = js.Dynamic.literal(
          pathname = AppRoutes.editWishesPath,
          state = js.Dynamic.literal()
        )
        props.push(AppRoutes.editWishesPath)
      case Failure(err) => System.err.println(s"Failed to log in: ${err.getMessage}")
    }

  }

  private def cardContent =
    CardContent(
      form(onSubmit := handleSubmit)(
        TextField(label = "E-mail",
                  autoFocus = true,
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
