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

// client-side implementation, and call-site
object AuthClient extends autowire.Client[String, Decoder, Encoder] {

  private var token: Option[SessionToken] = None
  def setToken(sessionToken: SessionToken): Unit =
    token = Option(sessionToken)

  def write[Result: Encoder](r: Result) = r.asJson.spaces2
  def read[Result: Decoder](p: String) = {
    println(s"Decoding ${p}")
    println(s"Decoding ${parse(p)}")
    decode[Result](p).right.get
  }
  override def doCall(req: Request): Future[String] = {
    val json = req.args.mapValues(v => v.drop(1).dropRight(1)).asJson.spaces2
    println(s"Sending json ${json}")
    Ajax
      .post("/authed/api/" + req.path.mkString("/"),
            json,
            headers = Map(
              "sessiontoken" -> token.get
            ))
      .map(_.responseText)
      .map { r =>
        println(r)
        r
      }
  }
}
object Client extends autowire.Client[String, Decoder, Encoder] {

  def write[Result: Encoder](r: Result) = r.asJson.spaces2
  def read[Result: Decoder](p: String) = {
    println(s"Decoding ${p}")
    println(s"Decoding ${parse(p)}")
    decode[Result](p).right.get
  }
  override def doCall(req: Request): Future[String] = {
    val json = req.args.mapValues(v => v.drop(1).dropRight(1)).asJson.spaces2
    println(s"Sending json ${json}")
    Ajax
      .post("/api/" + req.path.mkString("/"), json)
      .map { response =>
        println(response.getAllResponseHeaders())
        println(response.getResponseHeader("Cookie"))
        println(response.getResponseHeader("Set-Cookie"))
        response
      }
      .map(_.responseText)
      .map { r =>
        println(r)
        r
      }
  }
}

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
