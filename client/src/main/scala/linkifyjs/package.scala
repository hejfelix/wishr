import slinky.core.ExternalComponent
import slinky.core.annotations.react

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.{|, UndefOr}

package object linkifyjs {

  type Callback = js.Function2[String, String, Unit]

  trait Target {
    def url: String
  }

  trait Options {
    def attributes: UndefOr[js.Any | Callback]
    def className: String
    def defaultProtocol: String
    def events: js.Any | Callback
    def format: js.Any | Callback

    def formatHref: js.Any | Callback

    def ignoreTags: List[String]
    def nl2br: Boolean
    def tagName: String | Callback | js.Any
    def target: Target
    def validate: Boolean
  }

  @js.native
  @JSImport("linkifyjs/html", JSImport.Default)
  object linkifyHtml extends js.Function2[String, UndefOr[Options], String] {
    override def apply(arg1: String, arg2: UndefOr[Options] = js.undefined): String = js.native
  }
  @JSImport("linkifyjs/react", JSImport.Default)
  @js.native
  object LinkifyComponents extends js.Object

  @react object Linkify extends ExternalComponent {
    case class Props(tagName: UndefOr[String] = js.undefined,
                     options: UndefOr[Options] = js.undefined)
    override val component = LinkifyComponents
  }
}
