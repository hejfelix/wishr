package com.lambdaminute.wishr.component

import chandu0101.scalajs.react.components.materialui.Mui.SvgIcons.ImageControlPoint
import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.component.WishRAppContainer.{Action, Page, Primary, Secondary}
import com.lambdaminute.wishr.model.Wish
import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactNode, _}

object EditWishesPage {

  val component: ReactComponentC.ReqProps[Props, State, Backend, TopNode] =
    ReactComponentB[Props]("UserCard")
      .initialState(EditWishesPage.State())
      .renderBackend[EditWishesPage.Backend]
      .build

  def apply(user: String,
            wishes: List[Wish],
            secret: String,
            showSnackBar: String => Unit,
            editingWishes: List[Wish],
            startEditing: Wish => Unit,
            stopEditing: (Wish, Wish) => Unit,
            updateWishes: (List[Wish] => List[Wish]) => Unit,
            grantWish: Wish => Unit,
            showDialog: (String, Option[Page], List[Action]) => Unit) =
    component(
      EditWishesPage.Props(
        user,
        secret,
        wishes,
        showSnackBar,
        editingWishes,
        startEditing,
        stopEditing,
        updateWishes,
        grantWish,
        showDialog
      ))

  case class Props(owner: String,
                   secret: String,
                   wishes: List[Wish],
                   showSnackBar: String => Unit,
                   editingWishes: List[Wish],
                   startEditing: Wish => Unit,
                   stopEditing: (Wish, Wish) => Unit,
                   updateWishes: (List[Wish] => List[Wish]) => Unit,
                   grantWish: Wish => Unit,
                   showDialog: (String, Option[Page], List[Action]) => Unit)

  case class State()

  class Backend($ : BackendScope[Props, State]) {

    def addWish(w: Wish)(wishes: List[Wish]): List[Wish] =
      if (wishes.contains(w))
        wishes
      else
        w :: wishes

    def handleAddWish: ReactEventH => Callback =
      e => {
        val defaultWish = Wish("", "", None)
        val props       = $.props.runNow()
        Callback(props.updateWishes(addWish(defaultWish))) >> Callback(
          props.startEditing(defaultWish))
      }

    def handleDialogCancel: ReactEventH => Callback =
      e => Callback.info("Cancel Clicked")

    def handleDialogSubmit: ReactEventH => Callback =
      e => Callback.info("Submit Clicked")

    val handleClose: Boolean => Callback =
      b => Callback.info(s"onRequestClose: $b")

    def handleDelete(w: Wish): ReactEventH => Callback =
      e => Callback.info("Delete clicked")

    val deleteDialogText =
      "Are you sure you want to delete this wish? Accept by selecting a reason below"

    def dropFirstMatch[T](l: List[T], t: T) =
      l.takeWhile(_ != t) ++ l.dropWhile(_ != t).drop(1)

    def render(S: State, P: Props) = {

      lazy val cards =
        P.wishes.zip((0 to P.wishes.size - 1).reverse).map {
          case (w, i) =>
            val deleteAction =
              Action(
                "Wish granted",
                Callback(P.grantWish(w)) >> Callback(P.updateWishes(l => dropFirstMatch(l, w))),
                level = Primary) ::
                Action("No longer a wish",
                       Callback(P.updateWishes(l => dropFirstMatch(l, w))),
                       level = Secondary) ::
                Nil
            val deleteCallback =
              Callback(P.showDialog(deleteDialogText, None, deleteAction))

            <.div(
              ^.key := i,
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
            )
        }

      lazy val wishCards = <.div(
        ^.cls := "CardsList",
        ReactCssTransitionGroup("wish", component = "div")(cards)
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
