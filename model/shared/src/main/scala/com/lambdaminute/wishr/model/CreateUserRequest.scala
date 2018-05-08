package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.Email

case class CreateUserRequest(firstName: String,
  lastName: String,
  email: Email,
  password: String)