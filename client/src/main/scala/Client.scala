import io.circe.parser.parse
import io.circe.{Decoder, Encoder}
import org.scalajs.dom.ext.{Ajax, AjaxException}

import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import autowire._
import io.circe.syntax._
import io.circe.parser._
import io.circe.generic.auto._

import scala.util.Failure
object Client
    extends autowire.Client[String, Decoder, Encoder]
    with ClientErrorCallback
    with FutureRetry {

  def write[Result: Encoder](r: Result) = r.asJson.spaces2

  def read[Result: Decoder](p: String) = {
    println(s"Decoding ${p}")
    println(s"Decoding ${parse(p)}")
    println(s"Decoding ${decode[Result](p)}")
    decode[Result](p).right.get
  }

  override def doCall(req: Request): Future[String] = {
    val json = req.args
      .map {
        case (k, valueAsJson) => s""""$k" : $valueAsJson"""
      }
      .mkString("{", ",", "}")
    println(s"Request as json: ${json}")
    val future = retry(Ajax
                         .post("/api/" + req.path.mkString("/"), json),
                       10,
                       Option(req.path.mkString("/")))
      .map(_.responseText)
      .map { r =>
        println(r)
        r
      }

    future.onComplete {
      case Failure(ajax: AjaxException) =>
        println(s"Ajax exceptioN: ${ajax.xhr.status}")
        onError(ajax)
      case Failure(throwable) =>
        System.err.println(throwable.getMessage)
        throwable.printStackTrace()
      case x => println(s"Unauthed success :D ${x}")
    }
    future
  }
}
