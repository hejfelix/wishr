package com.lambdaminute.wishr

import japgolly.scalajs.react.{
  ReactComponentB,
  ReactComponentU,
  ReactDOM,
  TopNode
}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.document
import org.scalajs.dom.raw.NodeList
import org.scalajs.jquery.{JQuery, jQuery}

import scala.scalajs.js.JSApp

object MyReactComponent extends JSApp {

  val numberComponent =
    ReactComponentB[Int]("MyComponent")
      .render_P(i => <.p(s"Here is your number: $i"))
      .build

  def main(): Unit = {
    val domRoot: JQuery = jQuery("#wishr-app")
    println(domRoot.get(0))
    ReactDOM.render(numberComponent(42), domRoot.apply(0))
  }

}
