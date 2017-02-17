package com.lambdaminute.wishr

import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.component.{EditWishesPage, LoginPage, WishRAppContainer}
import com.lambdaminute.wishr.model.{Wish, WishList}
import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactDOM}
import org.scalajs.dom.{XMLHttpRequest, document}

import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope
//import upickle.default._

import org.scalajs.dom.ext.Ajax
import scala.scalajs.js.JSApp

import serialization.OptionPickler._

@js.native
@JSGlobalScope
object Conf extends js.Object {
  val conf:js.Dictionary[String] = scalajs.js.native
}

object WishRRootComponent extends JSApp {

  val user = "Felix Palludan Hargreaves"

  def main(): Unit = {

    val domRoot = document.getElementById("wishr-app")
    ReactDOM.render(WishRAppContainer().build(), domRoot)

  }

}
