package com.lambdaminute.wishr.component

import chandu0101.scalajs.react.components.materialui.Mui.SvgIcons.ImageControlPoint
import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.component.WishRAppContainer.{Action, Page}
import com.lambdaminute.wishr.model.Wish
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactNode, _}
object EditWishesPage {

  def apply(user: String,
            wishes: List[Wish],
            secret: String,
            showSnackBar: String => Unit,
            editingWishes: List[Wish],
            startEditing: Wish => Unit,
            stopEditing: (Wish, Wish) => Unit,
            updateWishes: (List[Wish] => List[Wish]) => Unit,
            showDialog: (String, Option[Page], List[Action]) => Unit) =
    ReactComponentB[EditWishesPage.Props]("UserCard")
      .initialState(EditWishesPage
        .State())
      .renderBackend[EditWishesPage.Backend]
      .propsDefault(
        EditWishesPage
          .Props(user,
                 secret,
                 wishes,
                 showSnackBar,
                 editingWishes,
                 startEditing,
                 stopEditing,
                 updateWishes,
                 showDialog))

  case class Props(owner: String,
                   secret: String,
                   wishes: List[Wish],
                   showSnackBar: String => Unit,
                   editingWishes: List[Wish],
                   startEditing: Wish => Unit,
                   stopEditing: (Wish, Wish) => Unit,
                   updateWishes: (List[Wish] => List[Wish]) => Unit,
                   showDialog: (String, Option[Page], List[Action]) => Unit)

  case class State()

  class Backend($ : BackendScope[Props, State]) {

    def addWish(w: Wish, inEditMode: Boolean = false)(wishes: List[Wish]): List[Wish] =
      if (wishes.contains(w))
        wishes
      else
        w :: wishes

    def handleAddWish: ReactEventH => Callback =
      e =>
        Callback(
          $.props.runNow().updateWishes(addWish(Wish("New Wish", "Description", None), true)))

    def handleDialogCancel: ReactEventH => Callback =
      e => Callback.info("Cancel Clicked")

    def handleDialogSubmit: ReactEventH => Callback =
      e => Callback.info("Submit Clicked")

    val handleClose: Boolean => Callback =
      b => Callback.info(s"onRequestClose: $b")

    def handleDelete(w: Wish): ReactEventH => Callback =
      e => Callback.info("Delete clicked")

    val deleteDialogText = "Are you sure you want to delete this wish? Deleting cannot be undone"

    def dropFirstMatch[T](l: List[T], t: T) =
      l.takeWhile(_ != t) ++ l.dropWhile(_ != t).drop(1)

    def render(S: State, P: Props) = {

      lazy val cards =
        P.wishes.zipWithIndex.map {
          case (w, i) =>
            val deleteAction =
              Action("Delete", Callback(P.updateWishes(l => dropFirstMatch(l, w)))) :: Nil
            val deleteCallback =
              Callback(P.showDialog(deleteDialogText, None, deleteAction))
            WishCard.fromWish(
              w,
              deleteCallback >> Callback.info(s"Deleting wish: $w"),
              i,
              P.editingWishes.contains(w),
              Callback(P.startEditing(w)) >> Callback.info(s"Started editing $w"),
              newWish =>
                Callback(P.stopEditing(w, newWish)) >> Callback.info(
                  s"Changing wish to ${newWish}")
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

      lazy val title = <.h2(s"Welcome to the wish list of ${P.owner}")(^.cls := "edit-page-title")

      lazy val addWishButton =
        MuiFloatingActionButton(key = "floating1",
                                onMouseUp = handleAddWish,
                                className = "add-wish")(
          ImageControlPoint(
            )())

      <.div(_react_fragReactNode(addWishButton), <.div(title, wishCards))
    }
  }

}
