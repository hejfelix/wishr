package com.lambdaminute.wishr

import com.lambdaminute.wishr.model.tags._
import doobie.util.meta.Meta

package object persistence {

  implicit val metaEmail: Meta[Email]         = Meta[String].xmap(_.asEmail, _.toString)
  implicit val metaSecretUrl: Meta[SecretUrl] = Meta[String].xmap(_.asSecretUrl, _.toString)


}
