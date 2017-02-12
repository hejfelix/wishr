package com.lambdaminute.wishr

import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.component.EditWishesPage
import com.lambdaminute.wishr.model.WishList
import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactDOM}
import org.scalajs.dom.document
//import upickle.default._

import scala.scalajs.js.JSApp

import serialization.OptionPickler._

object WishRRootComponent extends JSApp {

  def main(): Unit = {
    var i = 1
    class Backend($ : BackendScope[Unit, Vector[String]]) {
      def handleAdd = {
        i = i + 1
        $.modState(_ :+ s"bla$i")
      }

      def handleRemove(i: Int) =
        $.modState(_.zipWithIndex.filterNot(_._2 == i).map(_._1))

      def render(state: Vector[String]) =
        <.div(
          <.button(^.onClick --> handleAdd, "Add Item"),
          ReactCssTransitionGroup("example", component = "h1")(
            state.zipWithIndex.map {
              case (s, i) =>
                <.div(^.key := s, ^.onClick --> handleRemove(i), s)
            }: _*
          )
        )
    }

    val TodoList = ReactComponentB[Unit]("TodoList")
      .initialState(Vector("hello", "world", "click", "me"))
      .renderBackend[Backend]
      .build

    val theme: MuiTheme = Mui.Styles.getMuiTheme(Mui.Styles.LightRawTheme)

    def wishes: WishList = read[WishList](WishList.testString)

    val editWishesPage =
      ReactComponentB[EditWishesPage.Props]("UserCard")
        .initialState(EditWishesPage
          .State(false, wishes.owner, wishes.wishes, None, Nil, theme))
        .renderBackend[EditWishesPage.Backend]
        .propsDefault(EditWishesPage.Props(wishes.owner))
        .build()

    val domRoot = document.getElementById("wishr-app")
    ReactDOM.render(editWishesPage, domRoot)

//    val wishCard = ReactComponentB[WishCard.Props]("card")
//      .initialState(WishCard.State(Wish("bog", "den er sej", None)))
//      .renderBackend[WishCard.Backend]
//      .propsDefault(
//        WishCard.Props(
//          _ => Callback.empty,
//          _ => Callback.empty,
//          w => Callback.info(w.toString),
//          0,
//          true
//        )).build()
//
//    val domRoot = document.getElementById("wishr-app")
//    ReactDOM.render(MuiMuiThemeProvider(muiTheme = theme)(wishCard), domRoot)

  }

}
