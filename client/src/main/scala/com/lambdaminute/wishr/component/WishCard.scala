package com.lambdaminute.wishr.component

import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.model.Wish
import derive.key
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.{Frag, ReactTagOf}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.html.Image

import scala.scalajs
import scala.scalajs.js
import scala.scalajs.js.Any
import scalaz.Alpha.S
import chandu0101.scalajs.react.components.Implicits._
import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
object WishCard {

  case class State(wish: Wish)

  case class Props(openDeleteDialog: ReactEventH => Callback)

  def fromWish(wish: Wish, openDeleteDialog: Callback) =
    ReactComponentB[WishCard.Props]("WishCard")
      .initialState(WishCard.State(wish))
      .renderBackend[WishCard.Backend]
      .propsDefault {
        WishCard.Props(
          _ => openDeleteDialog
        )
      }
      .build

  class Backend($ : BackendScope[Props, State]) {

    def lookupIcon(name: String): MuiSvgIcon = {
      val lookup = Mui.SvgIcons.asInstanceOf[scalajs.js.Dynamic]
      lookup.selectDynamic(name).asInstanceOf[MuiSvgIcon]
    }

    def createImage(image: Option[String]): Frag = image match {
      case Some(url) => <.img(^.src := url)
      case None =>
        _react_fragReactNode(
          lookupIcon("ImagePhoto").apply(
            style = js.Dynamic.literal(width = "48px", height = "48px"))())
    }

    def render(S: State, props: Props) = {

      val wishCardContent = <.div(
        ^.cls := "WishCard-Content clearfix",
        <.div(^.cls := "WishCard-Content-Image", createImage(S.wish.image)),
        <.div(^.cls := "WishCard-Content-Description",
              <.h3(s"${S.wish.heading}"),
              _react_fragReactNode(s"${S.wish.desc}"))
      )

      val wishCardActions = <.div(
        ^.cls := "WishCard-Actions",
        <.hr(),
        MuiFlatButton(key = "edit", label = "Edit", primary = true)(),
        MuiFlatButton(key = "delete",
                      label = "Delete",
                      secondary = true,
                      onClick = props.openDeleteDialog)()
      )

      ReactCssTransitionGroup("wish-card", component = "h1")(

        MuiPaper(zDepth = ZDepth._4)(
          <.div(
            ^.cls := "WishCard",
            wishCardContent,
            wishCardActions
          )
        )
      )

    }

  }

}
