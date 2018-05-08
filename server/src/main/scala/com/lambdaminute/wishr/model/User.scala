package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.{Email, SessionToken}

case class User(name: Email, secret: SessionToken)