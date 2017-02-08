package com.lambdaminute.wishr.model

case class WishList ( password: String, wishes: List[Wish] )

case class Wish ( heading: String, desc: String, image: Option[String] )