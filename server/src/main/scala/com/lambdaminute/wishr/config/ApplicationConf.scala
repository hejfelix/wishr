package com.lambdaminute.wishr.config

import java.net.URI

case class ApplicationConf(dburl: String,
                           dbssl: Boolean,
                           port: Int,
                           rootPath: String,
                           emailSettings: EmailSettings)

case class EmailSettings(smtp: String, port: Int, user: String, password: String, sender: String)

object DBConfig {
  def fromStringUrl(url: String, ssl: Boolean) = {
    val dbUri    = new URI(url)
    val split = dbUri.getUserInfo().split(":")
    val username = split(0)
    val password = if (split.length > 1) split(1) else ""
    val dbUrl    = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
    DBConfig(dbUrl, username, password, ssl)
  }
}
case class DBConfig(url: String, user: String, password: String, ssl: Boolean)
