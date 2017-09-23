package com.lambdaminute.wishr

import cats.effect.Sync
import com.lambdaminute.wishr.config.DBConfig
import org.flywaydb.core.Flyway

package object db {

  def init[F[_]](conf: DBConfig)(implicit F: Sync[F]) = {
    F.delay {
      val flyway = new Flyway
      flyway.setDataSource(conf.url, conf.user, conf.password)
      val _ = flyway.migrate()
    }
  }

}
