package com.lambdaminute.wishr

import com.lambdaminute.wishr.component.WishRAppContainer
import japgolly.scalajs.react.ReactDOM
import org.scalajs.dom.document

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope

import scala.scalajs.js.JSApp

@js.native
@JSGlobalScope
object Conf extends js.Object {
  val conf: js.Dictionary[String] = scalajs.js.native
}

object WishRRootComponent extends JSApp {

  val user = "Felix Palludan Hargreaves"

  def main(): Unit = {

    val domRoot = document.getElementById("wishr-app")
    val kvps = document.cookie.split(";")
    println(kvps.mkString)
    val secret  = kvps.headOption.flatMap(_.split("=").drop(1).headOption)
    println(s"secret: $secret")
    val user  = kvps.drop(1).headOption.flatMap(_.split("=").drop(1).headOption)
    println(s"user: $user")
    ReactDOM.render(WishRAppContainer(Conf.conf.get("version").mkString, secret,user).build(), domRoot)

  }

}
