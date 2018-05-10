package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.{Email, Password}

case class LoginRequest(email: Email, password: Password)