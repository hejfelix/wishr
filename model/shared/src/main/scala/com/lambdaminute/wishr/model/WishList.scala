package com.lambdaminute.wishr.model

object WishList {

  val testString =
    """|{
       |  "owner" : "Felix Palludan Hargreaves",
       |  "password":"34c91db1b0b0ab048507cb3592ae700b",
       |  "wishes":[
       |    {
       |      "heading":"Stol",
       |      "desc":"Det er den fedeste stol ever",
       |      "image": "http://images.crateandbarrel.com/is/image/Crate/GiaChairTealSHF15_16x9/$web_zoom_furn_hero$/150617162035/gia-chair.jpg"
       |    },
       |    {
       |      "heading": "Bord",
       |      "desc": "Det er det fedeste bord ever",
       |      "image": null
       |    },
       |    {
       |      "heading":"TV",
       |      "desc":"Det er det fedeste TV",
       |      "image": null
       |    }
       |  ]
       |}""".stripMargin

}

case class WishList(owner: String, password: String, wishes: List[Wish])

case class Wish(heading: String, desc: String, image: Option[String])
