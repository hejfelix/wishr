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
import com.lambdaminute.wishr.serialization
import japgolly.scalajs.react.ReactComponentC.DefaultProps
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax

import scalacss.mutable.StyleSheet
import serialization.OptionPickler._

import concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
object EditWishesPage {

  case class Props(owner: String)

  case class State(deleteDialogIsOpen: Boolean,
                   user: String,
                   wishes: List[Wish],
                   selectedWish: Option[Wish],
                   editingWishes: List[Wish],
                   theme: MuiTheme,
                   snackBarOpen: Boolean = false)

  def dropFirstMatch[T](l: List[T], t: T) =
    l.takeWhile(_ != t) ++ l.dropWhile(_ != t).drop(1)

  class Backend($ : BackendScope[Props, State]) {

    val closeSnackBar = $.modState(_.copy(snackBarOpen = false))
    val openSnackBar  = $.modState(_.copy(snackBarOpen = true))

    val closeRequested: String => Callback =
      reason => closeSnackBar >> Callback.info(s"onRequestClose: $reason")

    def persist(state: State): State = {
      Ajax
        .post(s"./${state.user}/set",
              write[List[Wish]](state.wishes),
              headers = Map("Content-Type" -> "application/json"))
        .map(_.responseText)
        .onComplete {
          case Success(msg) =>
            println(s"Succesfully persisted state: $msg")
            openSnackBar.runNow()
          case Failure(err) => println(s"Error persisting state: $err")
        }
      state
    }

    def changeWish(from: Wish, to: Wish)(state: State): State =
      persist(
        state.copy(
          wishes = state.wishes.takeWhile(_ != from) ++ (to :: state.wishes
              .dropWhile(_ != from)
              .drop(1))))

    def removeSelectedAndClose(s: State): State =
      s.selectedWish match {
        case Some(w) =>
          persist(s.copy(deleteDialogIsOpen = false, wishes = dropFirstMatch(s.wishes, w)))
        case None => s
      }
    def addWish(w: Wish, inEditMode: Boolean = false)(s: State): State =
      if (s.wishes.contains(w))
        s
      else if (inEditMode)
        s.copy(
          wishes = w :: s.wishes,
          editingWishes = w :: s.editingWishes
        )
      else
        s.copy(
          wishes = w :: s.wishes
        )

    def openAndSelect(w: Wish): Callback =
      $.modState(s => s.copy(deleteDialogIsOpen = true, selectedWish = Option(w))) >> Callback
        .info(
          s"Opened delete dialog and " +
            s"selected $w")

    val close: Callback = $.modState(s => s.copy(deleteDialogIsOpen = false))

    def handleAddWish: ReactEventH => Callback =
      e => $.modState(addWish(Wish("New Wish", "Description", None), true))

    def handleDialogCancel: ReactEventH => Callback =
      e => close >> Callback.info("Cancel Clicked")

    def handleDialogSubmit: ReactEventH => Callback =
      e => $.modState(removeSelectedAndClose) >> Callback.info("Submit Clicked")

    val handleClose: Boolean => Callback =
      b => close >> Callback.info(s"onRequestClose: $b")

    def handleDelete(w: Wish): ReactEventH => Callback =
      e => $.modState(removeSelectedAndClose)

    def startEditing(w: Wish): Callback =
      $.modState(s => s.copy(editingWishes = w :: s.editingWishes))

    def stopEditingAndUpdate(w: Wish): Callback =
      $.modState(s => {
        val newEditing = dropFirstMatch(s.editingWishes, w)
        println(s"editing before ${s.editingWishes.map(_.heading)},  now: ${newEditing}")
        s.copy(editingWishes = newEditing)
      })

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
                stopEditingAndUpdate(w) >>
                  $.modState(changeWish(w, newWish)) >> Callback.info(
                  s"Changed state from $w to $newWish. ${S.editingWishes}")
            )
        }

      lazy val wishCards = <.div(
        ^.cls := "CardsList",
        cards
      )

      lazy val actions: ReactNode = List(
        MuiFlatButton(key = "1", label = "Cancel", primary = true, onTouchTap = handleDialogCancel)(),
        MuiFlatButton(key = "2", label = "Delete", secondary = true, onTouchTap = handleDialogSubmit)()
      )

      lazy val title = <.h2(s"Welcome to the wish list of ${P.owner}")(^.cls := "edit-page-title")

      lazy val addWishButton =
        MuiFloatingActionButton(key = "floating1", onMouseUp = handleAddWish, className = "add-wish")(
          ImageControlPoint(
            )())

      val snackBar = MuiSnackbar(
        autoHideDuration = 2500,
        message = "Wish list updated",
        onRequestClose = closeRequested,
        open = S.snackBarOpen
      )()

      MuiMuiThemeProvider(muiTheme = S.theme)(<.div(addWishButton, snackBar, <.div(title, deleteDialog, wishCards)))
    }
  }

}
