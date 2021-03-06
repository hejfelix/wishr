package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.Email

case class WishList(owner: Email, wishes: List[Wish])

case class Wish(heading: String, desc: String, image: Option[String], id: Int) {
  def isEmpty: Boolean =
    heading.isEmpty && desc.isEmpty && (image.isEmpty || image.mkString.isEmpty)
}
