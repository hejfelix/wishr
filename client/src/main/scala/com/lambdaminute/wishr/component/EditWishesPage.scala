package com.lambdaminute.wishr.component

import derive.key
import japgolly.scalajs.react.{
  BackendScope,
  Callback,
  ReactComponentB,
  ReactNode
}

import scala.scalajs
import scala.scalajs.js
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import chandu0101.scalajs.react.components.Implicits._
import com.lambdaminute.wishr.model.{Wish, WishList}

object EditWishesPage {

  case class Props(owner: String)

  case class State(deleteDialogIsOpen: Boolean,
                   wishes: WishList,
                   selectedWish: Option[Wish],
                   theme: MuiTheme)

  def removeSelectedAndClose(s: State): State =
    s.selectedWish match {
      case Some(w) =>
        s.copy(deleteDialogIsOpen = false,
               wishes =
                 s.wishes.copy(wishes = s.wishes.wishes.filterNot(_ == w)))
      case None => s
    }

  class Backend($ : BackendScope[Props, State]) {

    def openAndSelect(w: Wish): Callback =
      $.modState(s =>
        s.copy(deleteDialogIsOpen = true, selectedWish = Option(w))) >> Callback
        .info(
          s"Opened delete dialog and " +
            s"selected $w")

    val close: Callback = $.modState(s => s.copy(deleteDialogIsOpen = false))

    def handleDialogCancel: ReactEventH => Callback =
      e => close >> Callback.info("Cancel Clicked")

    def handleDialogSubmit: ReactEventH => Callback =
      e =>
        $.modState(removeSelectedAndClose) >> Callback.info("Submit Clicked")

    val handleClose: Boolean => Callback =
      b => close >> Callback.info(s"onRequestClose: $b")

    def handleDelete(w: Wish): ReactEventH => Callback =
      e => $.modState(removeSelectedAndClose)

    def render(S: State, P: Props) = {

      lazy val deleteDialog = MuiDialog(
        title = "Are you sure?",
        actions = actions,
        open = S.deleteDialogIsOpen,
        onRequestClose = handleClose
      )(
        "Deleting a wish cannot be undone"
      )

      lazy val cards =
        S.wishes.wishes.map(w => WishCard.fromWish(w, openAndSelect(w))())
      lazy val wishCards = <.div(
        ^.cls := "CardsList",
        cards
      )

      lazy val actions: ReactNode = List(
        MuiFlatButton(key = "1",
                      label = "Cancel",
                      primary = true,
                      onTouchTap = handleDialogCancel)(),
        MuiFlatButton(key = "2",
                      label = "Delete",
                      secondary = true,
                      onTouchTap = handleDialogSubmit)()
      )

      lazy val title = MuiPaper(zDepth = ZDepth._2)(
        <.h2(s"Welcome to the wish list of ${P.owner}")())

      MuiMuiThemeProvider(muiTheme = S.theme)(
        <.div(title, deleteDialog, wishCards))
    }
  }

}
