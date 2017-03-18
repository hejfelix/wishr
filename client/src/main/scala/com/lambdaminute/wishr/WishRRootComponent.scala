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

object WishRRootComponent extends JSApp {

  def cookieValue(key: String): Option[String] =
    document.cookie
      .split(";")
      .map(_.trim)
      .find(_.startsWith(key))
      .flatMap(_.split("=").drop(1).headOption)

  def main(): Unit = {

    val domRoot = document.getElementById("wishr-app")
    val kvps    = document.cookie.split(";")
    println(kvps.mkString)
    val auth   = cookieValue("authsecret")
    val user   = cookieValue("authname")
    println(s"user: $user")
    println(s"secret: $auth")
    ReactDOM.render(WishRAppContainer(Conf.conf.get("version").mkString, auth, user).build(),
                    domRoot)

  }

}

object SharedList extends JSApp {

  def main(): Unit = {

    val domRoot = document.getElementById("wishr-app")
    val kvps    = document.cookie.split(";")
    val secretURL =
      kvps.map(_.trim).find(_.startsWith("secretURL")).flatMap(_.split("=").drop(1).headOption)
    println(kvps.mkString("\n"))
    println(s"fetching secret url: $secretURL")
    Ajax
      .get(s"/shared-wishes/${secretURL.mkString}",
           headers = Map("Content-Type" -> "application/json"))
      .onComplete {
        case Success(msg) =>
          println(msg.responseText)
          val wishes = read[List[Wish]](msg.responseText)
          ReactDOM.render(SharedPage(wishes).build(), domRoot)
      }

  }

}
