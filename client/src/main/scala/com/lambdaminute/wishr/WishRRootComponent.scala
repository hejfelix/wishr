package com.lambdaminute.wishr

import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.component.EditWishesPage
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

    println(s"Fetching wishes for $user...")
    Ajax.get(s"./$user/entries", headers = Map("Content-Type" -> "application/json")).foreach { xhr =>
      val theme: MuiTheme = Mui.Styles.getMuiTheme(Mui.Styles.LightRawTheme)

      def wishes = read[List[Wish]](xhr.responseText)

      val editWishesPage =
        ReactComponentB[EditWishesPage.Props]("UserCard")
          .initialState(EditWishesPage
            .State(false, user, wishes, None, Nil, theme))
          .renderBackend[EditWishesPage.Backend]
          .propsDefault(EditWishesPage.Props(user))
          .build()

      val domRoot = document.getElementById("wishr-app")
      ReactDOM.render(editWishesPage, domRoot)

    }

  }

}
