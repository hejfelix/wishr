import com.lambdaminute.wishr.model.AuthedApi
import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.web.html.div

import scala.concurrent.Future
import scala.util.{Failure, Success}
import concurrent.ExecutionContext.Implicits.global
import autowire._
import com.lambdaminute.slinkywrappers.materialui.AlignContent.center
import com.lambdaminute.slinkywrappers.materialui.Grid
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`12` => twelve}
import com.lambdaminute.slinkywrappers.materialui.Direction._
import com.lambdaminute.slinkywrappers.materialui.Sizes.{`16` => sixteen}
import com.lambdaminute.wishr.model._
import com.lambdaminute.wishr.model.tags.SessionToken
import io.circe.generic.auto._
@react class EditPage extends Component {

  case class State(wishes: List[Wish] = List.empty, owner: Option[String] = None)
  case class Props(getWishes: () => Future[WishList], getMe: () => Future[UserInfo])

  override def initialState: State = State()

  def render() =
    div(
      Grid(container = true,
           direction = column,
           justify = center,
           spacing = sixteen,
           alignItems = center)(state.wishes.map(w =>
        Grid(xs = twelve, item = true).withKey(w.heading)(WishCard(w)))))

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
