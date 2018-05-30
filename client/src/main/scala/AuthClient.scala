import com.lambdaminute.wishr.model.tags._
import io.circe.parser.parse
import io.circe.{Decoder, Encoder}
import org.scalajs.dom.ext.Ajax

import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import autowire._
import io.circe.syntax._
import io.circe.parser._
import org.scalajs.dom.document
// client-side implementation, and call-site
object AuthClient extends autowire.Client[String, Decoder, Encoder] {

  private val tokenCookieKey = "sessionToken"

  private def cookieMap: Map[String, String] = document.cookie.split(";").map { kvpstring =>
    val (key, value) = kvpstring.span(_ != '=')
    key -> value.drop(1).mkString
  }.toMap

  private def token: Option[SessionToken] = {
    println(s"Getting token: ${document.cookie}")
    cookieMap.get(tokenCookieKey).map(_.asSessionToken)
  }

  def setToken(sessionToken: SessionToken): Unit = {
    println( document.cookie)
    document.cookie = s"$tokenCookieKey=$sessionToken;"
  }

  def write[Result: Encoder](r: Result) = r.asJson.spaces2
  def read[Result: Decoder](p: String) = {
    println(s"Decoding ${p}")
    println(s"Decoding ${parse(p)}")
    val result = decode[Result](p)
    println(result)
    result.right.get
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
