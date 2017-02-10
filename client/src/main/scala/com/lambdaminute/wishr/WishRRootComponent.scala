package com.lambdaminute.wishr

import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.component.UserCard.{Backend, State}
import com.lambdaminute.wishr.component.WishCard.{Backend, Props, State}
import com.lambdaminute.wishr.component.{EditWishesPage, UserCard, WishCard}
import com.lambdaminute.wishr.model.{Wish, WishList}
import com.sun.org.apache.xpath.internal.operations.Bool
import japgolly.scalajs.react
import japgolly.scalajs.react.ReactComponentC.BaseCtor
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{
  Callback,
  ReactComponentB,
  ReactComponentC,
  ReactComponentU,
  ReactDOM,
  TopNode
}
import org.scalajs.dom.document
import upickle.default._

import scala.scalajs.js.JSApp
import org.scalajs.core.tools.io.IO

import scalaz.Alpha.S

object WishRRootComponent extends JSApp {

  val testString =
    """|{
       |  "owner" : "Felix Palludan Hargreaves",
       |  "password":"34c91db1b0b0ab048507cb3592ae700b",
       |  "wishes":[
       |    {
       |      "heading":"Stol",
       |      "desc":"Det er den fedeste stol ever",
       |      "image": ["http://images.crateandbarrel.com/is/image/Crate/GiaChairTealSHF15_16x9/$web_zoom_furn_hero$/150617162035/gia-chair.jpg"]
       |    },
       |    {
       |      "heading": "Bord",
       |      "desc": "Det er det fedeste bord ever",
       |      "image": []
       |    },
       |    {
       |      "heading":"TV",
       |      "desc":"Det er det fedeste TV",
       |      "image": []
       |    }
       |  ]
       |}""".stripMargin

  def main(): Unit = {

    val theme: MuiTheme = Mui.Styles.getMuiTheme(Mui.Styles.LightRawTheme)

    def wishes: WishList = read[WishList](testString)

    val editWishesPage =
      ReactComponentB[EditWishesPage.Props]("UserCard")
        .initialState(EditWishesPage.State(false, wishes, None, theme))
        .renderBackend[EditWishesPage.Backend]
        .propsDefault(EditWishesPage.Props(wishes.owner))
        .build()

    val domRoot = document.getElementById("wishr-app")
    ReactDOM.render(editWishesPage, domRoot)

  }

}
