package com.lambdaminute.wishr.config

import com.lambdaminute.wishr.config.Module.ModuleOr
import com.lambdaminute.wishr.persistence.{Persistence, WeakPersistence}

case class PersistenceConfig(user: String, password: String) {
  def persistence: ModuleOr[Persistence] = Right(WeakPersistence())
}
