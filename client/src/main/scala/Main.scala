import com.lambdaminute.slinkywrappers.reactrouter._
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.parser.parse
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import slinky.web.ReactDOM
import com.lambdaminute.wishr.model.tags._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExportTopLevel




object Main {

  @JSExportTopLevel("entrypoint.main")
  def main(args: Array[String]): Unit = {

    val container = Option(dom.document.getElementById("root")).getOrElse {
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      elem
    }

    ReactDOM.render(BrowserRouter(withRouter(App)), container)
  }
}
