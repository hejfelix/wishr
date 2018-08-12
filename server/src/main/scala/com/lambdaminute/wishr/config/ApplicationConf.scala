package com.lambdaminute.wishr.config

case class ApplicationConf(dbconf: DBConfig, staticPath: String, port: Int)

case class EmailSettings(smtp: String, port: Int, user: String, password: String, sender: String)

case class DBConfig(user: String, password: String, url: String, driver: String)
