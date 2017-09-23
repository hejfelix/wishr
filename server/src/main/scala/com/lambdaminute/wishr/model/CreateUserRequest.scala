package com.lambdaminute.wishr.model

case class CreateUserRequest(firstName: String,
                             lastName: String,
                             email: String,
                             password: String)