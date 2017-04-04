package com.lambdaminute.wishr

import com.lambdaminute.wishr.component.{SharedPage, WishRAppContainer}
import com.lambdaminute.wishr.model.Wish
import japgolly.scalajs.react.ReactDOM
import org.scalajs.dom.document
import org.scalajs.dom.ext.Ajax

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope
import scala.scalajs.js.JSApp
import scala.util.Success
import com.lambdaminute.wishr.serialization.OptionPickler._
import concurrent.ExecutionContext.Implicits.global

@js.native
@JSGlobalScope
object Conf extends js.Object {
  val conf: js.Dictionary[String] = scalajs.js.native
}

trait CookieValue {
  def cookieValue(key: String): Option[String] =
    document.cookie
      .split(";")
      .map(_.trim)
      .find(_.startsWith(key))
      .flatMap(_.split("=").drop(1).headOption)
}

object WishRRootComponent extends JSApp with CookieValue {

  def main(): Unit = {

    val domRoot = document.getElementById("wishr-app")
    val kvps    = document.cookie.split(";")
    println(kvps.mkString)
    val auth = cookieValue("authsecret")
    val user = cookieValue("authname")
    println(s"user: $user")
    println(s"secret: $auth")
    ReactDOM.render(WishRAppContainer(Conf.conf.get("version").mkString, auth, user), domRoot)

  }

}

object SharedList extends JSApp with CookieValue {

  def main(): Unit = {

    val domRoot        = document.getElementById("wishr-app")
    val kvps           = document.cookie.split(";")
    val secretURL      = Conf.conf("secretURL")
    val secretURLOwner = Conf.conf("secretURLOwner")

    Ajax
      .get(s"/shared-wishes/${secretURL.mkString}",
           headers = Map("Content-Type" -> "application/json"))
      .onComplete {
        case Success(msg) =>
          println(msg.responseText)
          val wishes = read[List[Wish]](msg.responseText)
          ReactDOM.render(SharedPage(wishes, secretURLOwner.mkString).build(), domRoot)
      }

  }

}
