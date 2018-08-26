package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.SessionToken
import com.lambdaminute.wishr.model.tags.Email

case class User(email: Email, secret: SessionToken)