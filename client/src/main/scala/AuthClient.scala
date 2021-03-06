import com.lambdaminute.wishr.model.tags._
import io.circe.parser.parse
import io.circe.{Decoder, Encoder}
import org.scalajs.dom.ext.{Ajax, AjaxException}

import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import autowire._
import io.circe.syntax._
import io.circe.parser._
import org.scalajs.dom.{document, window}

import scala.util.{Failure, Success}
// client-side implementation, and call-site
object AuthClient
    extends autowire.Client[String, Decoder, Encoder]
    with ClientErrorCallback
    with FutureRetry {

  private val tokenCookieKey = "sessiontoken"

  private def cookieMap: Map[String, String] =
    document.cookie
      .split(";")
      .map { kvpstring =>
        val (key, value) = kvpstring.span(_ != '=')
        key.trim -> value.drop(1).mkString.trim
      }
      .toMap

  private def token: Option[SessionToken] = {
    println(s"Getting token: ${document.cookie}")
    println(cookieMap)
    cookieMap.get(tokenCookieKey).map(_.asSessionToken)
  }

  def isLoggedIn: Boolean = cookieMap.isDefinedAt(tokenCookieKey)

  def logOut: Unit =
    document.cookie = s"$tokenCookieKey=;expires=Thu, 01 Jan 1970 00:00:01 GMT"

  def setToken(sessionToken: SessionToken): Unit = {
    println(document.cookie)
    document.cookie = s"$tokenCookieKey=$sessionToken;"
    println(document.cookie)
  }

  def write[Result: Encoder](r: Result) = {
    println(s"Encoding r:${r}")
    r.asJson.spaces2
  }

  def read[Result: Decoder](p: String) = {
    println(s"Decoding ${p}")
    println(s"Decoding ${parse(p)}")
    val result = decode[Result](p)
    println(result)
    result.right.get
  }

  override def doCall(req: Request): Future[String] = {
    val json = req.args
      .map {
        case (k, valueAsJson) => s""""$k" : $valueAsJson"""
      }
      .mkString("{", ",", "}")
    println(s"Request as json: ${json}")
    val path = "/authed/api/" + req.path.mkString("/")
    val eventualResponse = retry(Ajax
                                   .post(path,
                                         json,
                                         headers = Map(
                                           "sessiontoken" -> token.get
                                         )),
                                 Option(path))

    eventualResponse.onComplete {
      case Success(req) =>
        println(req)
      case Failure(req: AjaxException) =>
        if (req.xhr.status == 403) {
          println("Unauthorized, redirecting to login....")
          logOut
          window.location.href = AppRoutes.loginPath
        }
        onError(req)
      case Failure(err) =>
        System.err.println(s"Request failed with unknown error: ${err.getMessage}")
    }

    eventualResponse
      .map(_.responseText)
      .map { r =>
        println(r)
        r
      }
  }
}
