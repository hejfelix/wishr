package com.lambdaminute.wishr.config

import org.h2.engine.Database


case class ApplicationConf(rootPath: String, databaseConfigKey: String, emailSettings: EmailSettings)

case class EmailSettings(smtp: String, port: Int,user: String, password: String, sender: String)
