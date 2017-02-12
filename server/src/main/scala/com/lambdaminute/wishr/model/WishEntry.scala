package com.lambdaminute.wishr.model

case class WishEntry(user: String,
                     heading: String,
                     desc: String,
                     image: Option[String])
