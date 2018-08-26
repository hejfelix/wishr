import org.scalajs.dom.ext.AjaxException
trait ClientErrorCallback {
  private var _onError: Option[AjaxException => Unit] = None
  def onError(throwable: AjaxException)               = _onError.foreach(_(throwable))
  def setErrorCallback(onError: AjaxException => Unit): Unit =
    this._onError = Option(onError)
}
