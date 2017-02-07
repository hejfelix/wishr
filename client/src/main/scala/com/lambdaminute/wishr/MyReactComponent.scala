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
import scalacss.mutable.GlobalRegistry
import org.scalajs.dom.document
import scalacss.ScalaCssReact._
import scalacss.Defaults._

case class State(isOpen: Boolean)

class Backend($ : BackendScope[_, State]) {

  val open = $.setState(State(true))
  val close = $.setState(State(false))

  def handleDialogCancel: ReactEventH => Callback =
    e => close >> Callback.info("Cancel Clicked")

  def handleDialogSubmit: ReactEventH => Callback =
    e => close >> Callback.info("Submit Clicked")

  val openDialog: ReactEventH => Callback =
    e => open >> Callback.info("Opened")

  val cb: UndefOr[(Boolean) => Callback] =
    ((x: Boolean) => Callback.log(s"Close touched button....$x"))

  object Style extends StyleSheet.Inline {
    import dsl._
    val paperContainer = style(display.flex,
                               flexWrap.wrap,
                               paddingTop(20.px),
                               unsafeChild("div")(
                                 margin(15 px),
                                 unsafeChild("p")(
                                   padding(15 px)
                                 )
                               ))
  }

  def render(S: State) = {
    val actions: ReactNode = js.Array(
      MuiFlatButton(key = "1",
                    label = "Cancel",
                    secondary = true,
                    onTouchTap = handleDialogCancel)(),
      MuiFlatButton(key = "2",
                    label = "Submit",
                    secondary = true,
                    onTouchTap = handleDialogSubmit)()
    )

    val dialog = MuiPaper()(
      MuiDialog(
        title = "Dialog With Actions",
        actions = actions,
        open = S.isOpen,
        onRequestClose = cb
      )(
        "Dialog example with floating buttons"
      ))

    MuiMuiThemeProvider()(
      MuiPaper(circle = true)(<.div(
        Style.paperContainer,
        dialog,
        MuiPaper(zDepth = ZDepth._1)(<.p("zDepth = 1")),
        MuiPaper(zDepth = ZDepth._2)(<.p("zDepth = 2")),
        MuiPaper(zDepth = ZDepth._3)(<.p("zDepth = 3")),
        MuiPaper(zDepth = ZDepth._4)(<.p("zDepth = 4")),
        MuiPaper(zDepth = ZDepth._5)(<.p("zDepth = 5")),
        MuiRaisedButton(label = "Dialog", onTouchTap = openDialog)()
      )))

  }
//

}

object MyReactComponent extends JSApp {

  val utheme: MuiTheme = Mui.Styles.getMuiTheme(Mui.Styles.LightRawTheme)

  val cb: UndefOr[(ReactTouchEvent) => Callback] =
    ((x: ReactTouchEvent) => Callback.log(s"Touched the button....$x"))

  val component = ReactComponentB[Unit]("WishRMuiAppBar")
    .render(
      P =>
        <.div(
          MuiMuiThemeProvider()(
            MuiAppBar(
              title = "WishR",
              onLeftIconButtonTouchTap = cb,
              showMenuIconButton = true
            )()
          )
      ))
    .build

  def apply(): ReactComponentU[Unit, Unit, Unit, TopNode] = component()

  def main(): Unit = {



    def userCardFor(name: String) =
      ReactComponentB[Unit]("UserCard")
        .initialState(UserCard.State(Mui.Styles.LightRawTheme, name))
        .renderBackend[UserCard.Backend]
        .build()

    val example = <.div( ^.cls := "CardsList",
                         userCardFor("Felix Palludan Hargreaves"),
                         userCardFor("Some Other Person"),
                         userCardFor("Yet Another Person"),
                         userCardFor("Benadryl Cucumberbatch Snitchkins")
         )

    val domRoot = document.getElementById("wishr-app")
    ReactDOM.render(example, domRoot)
  }

}
