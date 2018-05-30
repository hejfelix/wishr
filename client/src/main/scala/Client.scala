import io.circe.parser.parse
import io.circe.{Decoder, Encoder}
import org.scalajs.dom.ext.Ajax

import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import autowire._
import io.circe.syntax._
import io.circe.parser._
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
