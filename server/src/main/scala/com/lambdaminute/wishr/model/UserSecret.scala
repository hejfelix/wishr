package com.lambdaminute.wishr.model

import java.time.Instant


case class UserSecret(user: String, secretString: String, expirationDate: Instant)