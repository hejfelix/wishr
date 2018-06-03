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
    val json = req.args
      .map {
        case (k, valueAsJson) => s""""$k" : $valueAsJson"""
      }
      .mkString("{", ",", "}")
    println(s"Request as json: ${json}")
    Ajax
      .post("/api/" + req.path.mkString("/"), json)
      .map(_.responseText)
      .map { r =>
        println(r)
        r
      }
  }
}
