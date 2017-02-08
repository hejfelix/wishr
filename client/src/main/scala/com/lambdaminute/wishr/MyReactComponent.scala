package com.lambdaminute.wishr

import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.component.WishCard.{Backend, State}
import com.lambdaminute.wishr.component.{UserCard, WishCard}
import com.lambdaminute.wishr.model.{Wish, WishList}
import japgolly.scalajs.react
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, ReactComponentU, ReactDOM}
import org.scalajs.dom.document
import upickle.default._

import scala.scalajs.js.JSApp
import org.scalajs.core.tools.io.IO

object MyReactComponent extends JSApp {

  val testString =
    """|{
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
       |      "image": ["hej  2 "]
       |    },
       |    {
       |      "heading":"TV",
       |      "desc":"Det er det fedeste TV",
       |      "image": ["hejsa 3"]
       |    }
      |  ]
      |}""".stripMargin

  def wishes: WishList = read[WishList](testString)

  def main(): Unit = {
    println(testString)
    println(wishes)

    val theme = Mui.Styles.LightRawTheme

    def userCardFor(name: String) =
      ReactComponentB[Unit]("UserCard")
        .initialState(UserCard.State(theme, name))
        .renderBackend[UserCard.Backend]
        .build()

    def wishCard(wish: Wish) =
      ReactComponentB[Unit]("WishCard")
        .initialState(WishCard.State(theme, wish))
        .renderBackend[WishCard.Backend]
        .build()

    val example = <.div(
      ^.cls := "CardsList",
      List(wishes.wishes.map(wishCard))
    )


    val domRoot = document.getElementById("wishr-app")
    ReactDOM.render(example, domRoot)

  }

}
