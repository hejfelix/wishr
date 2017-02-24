package com.lambdaminute.wishr.persistence

case class DBUser(firstName: String,
                  lastName: String,
                  email: String,
                  hashedPassword: String,
                  registrationToken: String,
                  finalized: Boolean)
