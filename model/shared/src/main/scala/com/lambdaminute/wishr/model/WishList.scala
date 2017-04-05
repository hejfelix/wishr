package com.lambdaminute.wishr.model

case class WishList(owner: String, password: String, wishes: List[Wish])

case class Wish(heading: String, desc: String, image: Option[String]) {
  def isEmpty = heading.isEmpty && desc.isEmpty && image.isEmpty
}
