package com.lambdaminute.wishr.component

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

object UserCard {

  def forName(name: String) =
    ReactComponentB[Unit]("UserCard")
      .initialState(UserCard.State(name))
      .renderBackend[UserCard.Backend]
      .build()

  case class State(name: String)

  class Backend($ : BackendScope[_, State]) {
    def render(S: State) =
      MuiPaper(zDepth = ZDepth._2)(
        <.div(
          ^.cls := "UserCard",
          <.p(s"${S.name}"),
          MuiFlatButton(key = "edit", label = "Edit", primary = true)(),
          MuiFlatButton(key = "display", label = "Display", primary = true)()
        ))

  }

}
