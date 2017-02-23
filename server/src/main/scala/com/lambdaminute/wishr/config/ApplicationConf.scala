package com.lambdaminute.wishr.config

case class ApplicationConf(rootPath: String, emailSettings: EmailSettings)

case class EmailSettings(smtp: String, port: Int,user: String, password: String, sender: String)
