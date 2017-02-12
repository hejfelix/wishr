package com.lambdaminute.wishr.component

import chandu0101.scalajs.react.components.materialui._
import com.lambdaminute.wishr.model.Wish
import derive.key
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.{Frag, ReactTagOf}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.html.Image

import scala.scalajs
import scala.scalajs.js
import scala.scalajs.js.Any
import scalaz.Alpha.S
import chandu0101.scalajs.react.components.Implicits._
import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
object WishCard {

  case class State(wish: Wish)

  case class Props(openDeleteDialog: ReactEventH => Callback,
                   startEditing: ReactEventH => Callback,
                   onFinishedUpdate: Wish => Callback,
                   startingState: Wish,
                   index: Int,
                   editing: Boolean)

  def fromWish(wish: Wish,
               openDeleteDialog: Callback,
               index: Int,
               editing: Boolean,
               startEditing: Callback,
               onFinishedUpdate: Wish => Callback) =
    ReactComponentB[WishCard.Props]("WishCard")
      .initialState(WishCard.State(wish))
      .renderBackend[WishCard.Backend]
      .propsDefault {
        WishCard.Props(
          _ => openDeleteDialog,
          _ => startEditing,
          onFinishedUpdate,
          wish,
          index,
          editing
        )
      }
      .build()

  class Backend($ : BackendScope[Props, State]) {

    def handleTitleChange: ReactEventI => Callback =
      e => {
        e.persist()
        $.modState(s => {
          s.copy(wish = s.wish.copy(heading = e.target.value))
        })
      } >> Callback
        .info(s"new value for title: ${e.target.value}")

    def handleImageChange: ReactEventI => Callback =
      e => {
        e.persist()
        $.modState(s => {
          s.copy(wish = s.wish.copy(image = Option(e.target.value)))
        })
      } >> Callback
        .info(s"new value for image: ${e.target.value}")

    def handleDescriptionChange: ReactEventI => Callback =
      e => {
        e.persist()
        $.modState(s => {
          s.copy(wish = s.wish.copy(desc = e.target.value))
        })
      } >> Callback
        .info(s"new value for image: ${e.target.value}")

    def lookupIcon(name: String): MuiSvgIcon = {
      val lookup = Mui.SvgIcons.asInstanceOf[scalajs.js.Dynamic]
      lookup.selectDynamic(name).asInstanceOf[MuiSvgIcon]
    }

    def createImage(image: Option[String]): Frag = image match {
      case Some(url) => <.img(^.src := url)
      case None =>
        _react_fragReactNode(
          lookupIcon("ImagePhoto").apply(
            style = js.Dynamic.literal(width = "48px", height = "48px"))())
    }

    def render(S: State, props: Props) = {

      val wishCardContent = <.div(
        ^.cls := "WishCard-Content clearfix",
        <.div(^.cls := "WishCard-Content-Image", createImage(S.wish.image)),
        <.div(^.cls := "WishCard-Content-Description",
              <.h3(s"${S.wish.heading}"),
              _react_fragReactNode(s"${S.wish.desc}"))
      )

      val titleEditField = MuiTextField(floatingLabelText = "Title",
                                        defaultValue = S.wish.heading,
                                        onChange = handleTitleChange)()
      val descriptionEditField =
        MuiTextField(multiLine = true,
                     floatingLabelText = "Description",
                     defaultValue = S.wish.desc,
                     onChange = handleDescriptionChange)()

      val imageEditField =
        MuiTextField(floatingLabelText = "Image URL",
                     defaultValue = S.wish.image.mkString,
                     onChange = handleImageChange)()

      val editingContent = <.div(
        ^.cls := "WishCard-Content clearfix",
        <.div(^.cls := "WishCard-Content-Image", createImage(S.wish.image)),
        <.div(
          ^.cls := "WishCard-Content-Description",
          titleEditField,
          imageEditField
        ),
        <.div(
          ^.cls := "WishCard-Content-Description",
          descriptionEditField
        )
      )

      val wishCardActions = <.div(
        ^.cls := "WishCard-Actions",
        <.hr(),
        MuiFlatButton(key = "edit",
                      label = "Edit",
                      primary = true,
                      onClick = props.startEditing)(),
        MuiFlatButton(key = "delete",
                      label = "Delete",
                      secondary = true,
                      onClick = props.openDeleteDialog)()
      )

      val editCardActions = <.div(
        ^.cls := "WishCard-Actions",
        <.hr(),
        MuiFlatButton(key = "Finish",
                      label = "Finish",
                      primary = true,
                      onClick = (rh: ReactEventH) =>
                        props.onFinishedUpdate(
                          S.wish
                      ))(),
        MuiFlatButton(key = "Cancel",
                      label = "Cancel",
                      secondary = true,
                      onClick = (rh: ReactEventH) =>
                        props.onFinishedUpdate(props.startingState))()
      )

      println(s"Adding wish number ${props.index} with text ${S.wish.heading}")

      MuiPaper(zDepth = ZDepth._4,
               key = props.index.toString,
               transitionEnabled = true)(
        <.div(
          ^.cls := "WishCard",
          if (props.editing) editingContent else wishCardContent,
          if (props.editing) editCardActions else wishCardActions
        )
      )

    }

  }

}
