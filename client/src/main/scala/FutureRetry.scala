import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.AjaxException

import scala.concurrent.{ExecutionContext, Future}
trait FutureRetry {

  def retry(f: => Future[XMLHttpRequest], path: Option[String] = None, numRetries: Int = 20)(
      implicit ec: ExecutionContext): Future[XMLHttpRequest] =
    numRetries match {
      case 0 =>
        System.err.println("Request failed after 10 retries")
        f
      case i =>
        f.flatMap {
            case req if req.status == 400 => throw new AjaxException(req)
            case req                      => Future.successful(req)
          }
          .recoverWith {
            case t: AjaxException if t.xhr.status == 403 || t.xhr.status == 401 =>
              println(s"Token timed out, won't retry request...")
              Future.failed(t)
            case t: AjaxException =>
              System.err.println(
                s"Request failed: ${t.xhr.status}, ${t.xhr.responseText}. ${path.mkString}, retrying ${numRetries}")
              retry(f = f, path = path, numRetries = numRetries - 1)
            case t =>
              System.err.println(s"Future failed with exception: ${t}, retrying ${numRetries}")
              retry(f = f, path = path, numRetries = numRetries - 1)
          }
    }
}
