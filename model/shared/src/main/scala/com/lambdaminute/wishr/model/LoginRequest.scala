package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.Email

case class LoginRequest(email: Email, password: String)