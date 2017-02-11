package com.lambdaminute.wishr.component

import derive.key
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactNode}

import scala.scalajs
import scala.scalajs.js
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import chandu0101.scalajs.react.components.Implicits._
import com.lambdaminute.wishr.model.{Wish, WishList}
import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
import Mui.SvgIcons.ImageControlPoint
import com.lambdaminute.wishr.component.WishCard.{Backend, Props, State}
import japgolly.scalajs.react.ReactComponentC.DefaultProps

import scalacss.mutable.StyleSheet

object EditWishesPage {

  case class Props(owner: String)

  case class State(deleteDialogIsOpen: Boolean,
                   user: String,
                   wishes: List[Wish],
                   selectedWish: Option[Wish],
                   editingWishes: List[Wish],
                   theme: MuiTheme)

  def dropFirstMatch[T](l: List[T], t: T) =
    l.takeWhile(_ != t) ++ l.dropWhile(_ != t).drop(1)

  def removeSelectedAndClose(s: State): State =
    s.selectedWish match {
      case Some(w) =>
        s.copy(deleteDialogIsOpen = false,
               wishes = dropFirstMatch(s.wishes, w))
      case None => s
    }

  def changeWish(from: Wish, to: Wish)(state: State): State =
    state.copy(
      wishes = state.wishes.takeWhile(_ != from) ++ (to :: state.wishes
          .dropWhile(_ != from)
          .drop(1)))

  def addWish(w: Wish)(s: State): State = s.copy(wishes = w :: s.wishes)

  class Backend($ : BackendScope[Props, State]) {

    def openAndSelect(w: Wish): Callback =
      $.modState(s =>
        s.copy(deleteDialogIsOpen = true, selectedWish = Option(w))) >> Callback
        .info(
          s"Opened delete dialog and " +
            s"selected $w")

    val close: Callback = $.modState(s => s.copy(deleteDialogIsOpen = false))

    def handleAddWish: ReactEventH => Callback =
      e => $.modState(addWish(Wish("New Wish", "Description", None)))

    def handleDialogCancel: ReactEventH => Callback =
      e => close >> Callback.info("Cancel Clicked")

    def handleDialogSubmit: ReactEventH => Callback =
      e =>
        $.modState(removeSelectedAndClose) >> Callback.info("Submit Clicked")

    val handleClose: Boolean => Callback =
      b => close >> Callback.info(s"onRequestClose: $b")

    def handleDelete(w: Wish): ReactEventH => Callback =
      e => $.modState(removeSelectedAndClose)

    def startEditing(w: Wish): Callback =
      $.modState(s => s.copy(editingWishes = w :: s.editingWishes))

    def stopEditingAndUpdate(w: Wish): Callback =
      $.modState(s =>
        s.copy(editingWishes = dropFirstMatch(s.editingWishes, w)))

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
        S.wishes.zipWithIndex.map {
          case (w, i) =>
            WishCard.fromWish(
              w,
              openAndSelect(w),
              i,
              S.editingWishes.contains(w),
              startEditing(w),
              newWish =>
                $.modState(changeWish(w, newWish)) >> Callback.info(
                  s"Changed state from $w to $newWish")
            )
        }

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

      lazy val title = <.h2(s"Welcome to the wish list of ${P.owner}")(
        ^.cls := "edit-page-title")

//      object AddButtonStyle extends StyleSheet.Inline {
//
//        import dsl._
//
//        val container = style(maxWidth(1024 px))
//
//        val content = style(display.flex,
//                            padding(30.px),
//                            flexDirection.column,
//                            alignItems.center)
//      }

      lazy val addWishButton =
        MuiFloatingActionButton(key = "floating1",
                                onMouseUp = handleAddWish,
                                className = "add-wish")(
          ImageControlPoint(
            )())

      MuiMuiThemeProvider(muiTheme = S.theme)(
        <.div(addWishButton, <.div(title, deleteDialog, wishCards)))
    }
  }

}
