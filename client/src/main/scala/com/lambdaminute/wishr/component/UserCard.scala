package com.lambdaminute.wishr

import chandu0101.macros.tojs.GhPagesMacros
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, ReactDOM, TopNode, _}
import org.scalajs.jquery.{JQuery, jQuery}

import scala.scalajs.js.{JSApp, UndefOr}
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react.vdom.prefix_<^._
import chandu0101.scalajs.react.components.Implicits._

import scala.scalajs
import scala.scalajs.js
import scalacss.mutable.{GlobalRegistry, StyleSheet}
import org.scalajs.dom.document
import chandu0101.scalajs.react.components.Implicits._

import scalacss.Attrs
import scalacss.Attrs.{display, flexWrap}
import scalacss.ScalaCssReact._
import scalacss.Defaults._
import scalacss._
import scalacss.ScalaCssReact._
import scalacss.Defaults._

object UserCard {

  case class State(baseTheme: MuiRawTheme, name: String) {
    val theme: MuiTheme =
      Mui.Styles.getMuiTheme(baseTheme)
  }

  class Backend($ : BackendScope[_, State]) {
    def render(S: State) =
      MuiMuiThemeProvider(muiTheme = S.theme)(
        MuiPaper(zDepth = ZDepth._2)(
          <.div(
            ^.cls := "UserCard",
            <.p(s"${S.name}"),
            MuiFlatButton(key = "edit", label = "Edit", primary = true)(),
            MuiFlatButton(key = "display", label = "Display", primary = true)()
          )))

  }

}
