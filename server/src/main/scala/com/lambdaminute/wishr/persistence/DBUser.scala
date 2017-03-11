package com.lambdaminute.wishr.persistence

case class DBUser(firstName: String,
                  lastName: String,
                  email: String,
                  hashedPassword: String,
                  secretURL: String,
                  registrationToken: String,
                  finalized: Boolean)
