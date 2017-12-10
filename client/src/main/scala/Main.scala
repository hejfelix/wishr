import autowire._
import com.lambdaminute.wishr.model.Api
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import autowire._

// client-side implementation, and call-site
object MyClient extends autowire.Client[String, Decoder, Encoder] {

  def write[Result: Encoder](r: Result) = r.asJson.spaces2
  def read[Result: Decoder](p: String)  = decode[Result](p).right.get

  override def doCall(req: Request): Future[String] =
    Ajax
      .post("http://localhost:9000/api/" + req.path.mkString("/"), req.args.asJson.spaces2)
      .map(_.responseText)
}

@JSExportTopLevel("Main")
object Main {

  val root = "localhost:9000"

  @JSExportTopLevel("main")
  def main(args: Array[String]): Unit = {
    MyClient[Api].add(39, 3).call().foreach(println)
    Ajax
      .get(s"http://$root/hello/felix")
      .map(_.responseText + "DUDES")
      .foreach(println)
  }
}
