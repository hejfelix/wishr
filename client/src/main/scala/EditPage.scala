import com.lambdaminute.wishr.model.AuthedApi
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.web.html.div

import scala.concurrent.Future
import scala.util.{Failure, Success}
import concurrent.ExecutionContext.Implicits.global
import autowire._
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.model.tags.SessionToken
import io.circe.generic.auto._


@react class EditPage extends StatelessComponent {
  case class Props(getWishes: () => Future[WishList], getMe: () => Future[UserInfo])
  def render() = div()

  override def componentDidMount(): Unit = {
    super.componentDidMount()
    props.getMe().onComplete {
      case x => println(s"Get ME: ${x}")
    }
  }

}
