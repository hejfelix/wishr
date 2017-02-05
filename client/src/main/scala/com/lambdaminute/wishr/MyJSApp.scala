package com.lambdaminute.wishr


import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import org.scalajs.jquery.jQuery

object TutorialApp extends JSApp {

  def main(): Unit = {
    jQuery(setupUI _)
  }

  def setupUI(): Unit = {
    jQuery("#click-me-button").click(addClickedMessage _)
    jQuery("body").append("<p>Hello World</p>")
  }

  def addClickedMessage(): Unit = {
    jQuery("body").append("<p>You clicked the button!<p>")
  }
}
