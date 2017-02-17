package com.lambdaminute.wishr.config

import com.lambdaminute.wishr.config.Module.ModuleOr
import com.lambdaminute.wishr.persistence.{Persistence, WeakPersistence}

trait PersistenceConfig[Error, Secret] {
  def persistence: ModuleOr[Persistence[Error, Secret]]
}
case class WeakPersistenceConfig(user: String, password: String) extends PersistenceConfig[String, String]{
  override def persistence: ModuleOr[Persistence[String, String]] = Right(WeakPersistence())
}
