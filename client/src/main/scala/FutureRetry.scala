import org.scalajs.dom.ext.AjaxException

import scala.concurrent.{ExecutionContext, Future}
trait FutureRetry {

  def retry[T](f: => Future[T], numRetries: Int = 10, path: Option[String] = None)(
      implicit ec: ExecutionContext): Future[T] =
    numRetries match {
      case 0 => Future.failed(new Exception("Request failed after 10 retries"))
      case i =>
        f.recoverWith {
          case t: AjaxException =>
            System.err.println(
              s"Request failed: ${t.xhr.status}, ${t.xhr.responseText}. ${path.mkString}")
            retry(f, numRetries - 1)
          case t =>
            System.err.println(s"Future failed with exception: ${t}, retrying ${numRetries}")
            retry(f, numRetries - 1)
        }
    }
}
