package com.lambdaminute.wishr.config

case class ApplicationConf(rootPath: String, emailSettings: EmailSettings)

case class EmailSettings(user: String, password: String, sender: String)
