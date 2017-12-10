import autowire._
import com.lambdaminute.wishr.model.Api
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
// client-side implementation, and call-site
//object MyClient extends autowire.Client[String, Decoder, Encoder] {
//
//  def write[Result: Encoder](r: Result) = r.asJson.spaces2
//  def read[Result: Decoder](p: String)  = decode[Result](p).right.get
//
//  override def doCall(req: Request): Future[String] =
//    Ajax
//      .post("/api/" + req.path.mkString("/"), req.args.asJson.spaces2)
//      .map(_.responseText)
//}
@JSExportTopLevel("Main")
object Main {

  @JSExportTopLevel("main")
  def main(args: Array[String]): Unit =
    Ajax
      .get(s"/hello/felix")
      .map(_.responseText + "DUDES")
      .foreach(println)
}
