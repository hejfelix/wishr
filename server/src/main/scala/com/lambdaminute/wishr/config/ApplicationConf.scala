package com.lambdaminute.wishr.config

import java.net.URI

case class ApplicationConf(dburl: String,
                           port: Int,
                           rootPath: String,
                           emailSettings: EmailSettings)

case class EmailSettings(smtp: String, port: Int, user: String, password: String, sender: String)

object DBConfig {
  def fromStringUrl(url: String) = {
    val dbUri    = new URI(url)
    val username = dbUri.getUserInfo().split(":")(0)
    val password = dbUri.getUserInfo().split(":")(1)
    val dbUrl    = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
    DBConfig(dbUrl, username, password)
  }
}
case class DBConfig(url: String, user: String, password: String)
