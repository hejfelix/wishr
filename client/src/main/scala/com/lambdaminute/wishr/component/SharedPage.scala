package com.lambdaminute.wishr.component

import chandu0101.scalajs.react.components.materialui.{Mui, MuiMuiThemeProvider}
import com.lambdaminute.wishr.component.LoginPage.Props
import com.lambdaminute.wishr.model.{User, Wish}
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}

import japgolly.scalajs.react.vdom.prefix_<^._
object SharedPage {

  case class Props(wishes: List[Wish])

  def apply(wishes: List[Wish]) =
    ReactComponentB[Props]("LoginPage")
      .renderBackend[SharedPage.Backend]
      .propsDefault(Props(wishes))

  class Backend($ : BackendScope[Props, _]) {
    def render(P: Props) = {
      val wishCards = P.wishes.map(w => WishCard.fromWishReadOnly(w))
      MuiMuiThemeProvider(muiTheme = Mui.Styles.getMuiTheme(Mui.Styles.LightRawTheme))(
        <.div(wishCards)
      )
    }
  }

}
