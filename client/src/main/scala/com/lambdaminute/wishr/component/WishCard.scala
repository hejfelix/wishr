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
import scala.scalajs.js.{Any, URIUtils}
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
                   editing: Boolean,
                   readOnly: Boolean)

  def fromWishReadOnly(w: Wish): ReactComponentU[Props, State, Backend, TopNode] =
    fromWish(w, Callback.empty, 0, false, Callback.empty, _ => Callback.empty, true)
  def fromWish(wish: Wish,
               openDeleteDialog: Callback,
               index: Int,
               editing: Boolean,
               startEditing: Callback,
               onFinishedUpdate: Wish => Callback,
               readOnly: Boolean = false) =
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
          editing,
          readOnly
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

      val imageEditField: ReactComponentU_ =
        MuiTextField(floatingLabelText = "Image URL",
                     defaultValue = S.wish.image.mkString,
                     onChange = handleImageChange)()

      val editingContent = <.div(
        ^.cls := "WishCard-Content clearfix",
        <.div(^.cls := "WishCard-Content-Image", createImage(S.wish.image)),
        <.div(
          ^.cls := "WishCard-Content-Description",
          _react_fragReactNode(titleEditField),
          imageEditField
        ),
        <.div(
          ^.cls := "WishCard-Content-Description",
          descriptionEditField
        )
      )

      val priceRunnerURLEncoded =
        URIUtils.encodeURI(s"http://www.pricerunner.dk/search?q=${S.wish.heading}")
      val amazonURLEncoded =
        URIUtils.encodeURI(s"https://www.amazon.co.uk/s/?field-keywords=${S.wish.heading}")

      println(priceRunnerURLEncoded)

      val amazonSearchButton =
        MuiFlatButton(label = "",
                      onClick = (_: ReactEventH) =>
                        Callback(
                          org.scalajs.dom.window.open(url = amazonURLEncoded, target = "_blank")
                      ))(
          <.img(
            ^.src := "./graphics/amazon.svg",
            ^.height := "32px",
            ^.width := "32px"
          )
        )
      val priceRunnerSearchButton =
        MuiFlatButton(
          label = "",
          onClick = (_: ReactEventH) =>
            Callback(
              org.scalajs.dom.window.open(url = priceRunnerURLEncoded, target = "_blank")
          ))(
          <.img(
            ^.src := "./graphics/pricerunner.svg",
            ^.height := "32px",
            ^.width := "32px"
          )
        )

      val searchButtons = <.div(
        ^.cls := "WishCard-Search",
        amazonSearchButton,
        priceRunnerSearchButton
      )

      lazy val wishCardActions = <.div(
        ^.cls := "WishCard-Actions",
        <.hr(),
        MuiFlatButton(key = "edit",
                      label = "Edit",
                      primary = true,
                      onClick = props.startEditing)(),
        MuiFlatButton(key = "delete",
                      label = "Delete",
                      secondary = true,
                      onClick = props.openDeleteDialog)(),
        searchButtons
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
                      onClick = (rh: ReactEventH) => props.onFinishedUpdate(props.startingState))()
      )

      lazy val readOnlyAction = <.div(
        ^.cls := "WishCard-Actions",
        <.hr(),
        <.div(^.cls := "WishCard-Actions",
              MuiFlatButton(key = "", label = "", primary = true)(),
              searchButtons)
      )

      val readOnlyContent = <.div(^.cls := "WishCard", wishCardContent, readOnlyAction)

      val editableContent = <.div(
        ^.cls := "WishCard",
        if (props.editing) editingContent else wishCardContent,
        if (props.editing) editCardActions else wishCardActions
      )

      val content =
        if (props.readOnly) readOnlyContent
        else editableContent

      MuiPaper(zDepth = ZDepth._4, key = props.index.toString, transitionEnabled = true)(
        content
      )

    }

  }

}
