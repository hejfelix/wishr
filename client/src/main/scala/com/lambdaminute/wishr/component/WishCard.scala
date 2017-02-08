package com.lambdaminute.wishr.component


import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.model.Wish
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import scalaz.Alpha.S

object WishCard {

  case class State(baseTheme: MuiRawTheme, wish: Wish) {
    val theme: MuiTheme =
      Mui.Styles.getMuiTheme(baseTheme)
  }

  class Backend($ : BackendScope[_, State]) {

    def render(S: State) =
      MuiMuiThemeProvider(muiTheme = S.theme)(
        MuiPaper(zDepth = ZDepth._2)(
          <.div(
            ^.cls := "WishCard",
            <.h3(s"${S.wish.heading}"),
            <.p(<.img(^.src := S.wish.image.mkString), s"${S.wish.desc}" ),
            MuiFlatButton(key = "edit", label = "Edit", primary = true)(),
            MuiFlatButton(key = "delete", label = "Delete", secondary = true)()
          )
        )
      )

  }

}
