package com.lambdaminute.wishr.model

import com.lambdaminute.wishr.model.tags.{Email, Password}

case class CreateUserRequest(firstName: String,
                             lastName: String,
                             email: Email,
                             password: Password)