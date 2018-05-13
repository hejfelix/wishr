import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import slinky.web.ReactDOM

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExportTopLevel

// client-side implementation, and call-site
object MyClient extends autowire.Client[String, Decoder, Encoder] {

  def write[Result: Encoder](r: Result) = r.asJson.spaces2
  def read[Result: Decoder](p: String)  = decode[Result](p).right.get

  override def doCall(req: Request): Future[String] =
    Ajax
      .post("/api/" + req.path.mkString("/"), req.args.asJson.spaces2)
      .map(_.responseText)
}

object Main {

  @JSExportTopLevel("entrypoint.main")
  def main(args: Array[String]): Unit = {

//    MyClient[AuthedApi[Id]].me().call().onComplete(println)

    val container = Option(dom.document.getElementById("root")).getOrElse {
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      elem
    }

    ReactDOM.render(App(), container)
  }
}
