package com.lambdaminute

import java.io.File

import com.lambdaminute.wishr.model.{Wish, WishEntry, WishList}
import com.lambdaminute.wishr.persistence.Persistence
import fs2.Task
import io.circe.Printer
import io.circe.generic.auto._
import io.getquill._
import org.http4s._
import org.http4s.circe.CirceInstances
import org.http4s.dsl._

case class WishRService(persistence: Persistence) extends CirceInstances {

  def serveFile(path: String, request: Request) =
    StaticFile
      .fromFile(new File(path), Some(request))
      .map(Task.now) // This one is require to make the types match up
      .getOrElse(NotFound()) // In case the file doesn't exist

  def service = HttpService {

    case request @ (GET -> Root) =>
      println("Serving index.html")
      serveFile("./index.html", request)

    case GET -> Root / user / "entries" =>
      val entries = persistence.getEntriesFor(user)

      val wishes: List[Wish] = entries.map {
        case WishEntry(_, heading, desc, image) =>
          Wish(heading, desc, image)
      }

      Ok(wishes)(jsonEncoderOf[List[Wish]])

    case request @ (POST -> Root / user / "set") =>
      val wishes: List[Wish] = request.as(jsonOf[List[Wish]]).unsafeRun()

      val entries = wishes.map {
        case Wish(heading, desc, image) =>
          WishEntry(user, heading, desc, image)
      }

      println(s"Setting wishes for $user: ${wishes.mkString}" )

      val addResult: String = persistence.set(entries)
      Ok(addResult)

    case request
        if request.method == GET && List(".css", ".html", ".js").exists(
          request.pathInfo.endsWith) =>
      println(s"Got static file request: ${request.pathInfo}")
      serveFile("." + request.pathInfo, request)
  }

  override protected def defaultPrinter: Printer = Printer.spaces2

}
