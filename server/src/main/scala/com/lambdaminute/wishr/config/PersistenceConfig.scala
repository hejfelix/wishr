package com.lambdaminute.wishr.config

import com.lambdaminute.wishr.config.Module.ModuleOr
import com.lambdaminute.wishr.persistence.{
  InMemoryH2,
  Persistence,
  PostgreSQLPersistence,
  WeakPersistence
}

trait PersistenceConfig[Error, Secret] {
  def persistence: ModuleOr[Persistence[Error, Secret]]
}
case class WeakPersistenceConfig(user: String, password: String)
    extends PersistenceConfig[String, String] {
  override def persistence: ModuleOr[Persistence[String, String]] = Right(WeakPersistence())
}

case class InMemoryPersistenceConfig(dbConfigKey: String, port: Int)
    extends PersistenceConfig[String, String] {
  override def persistence: ModuleOr[Persistence[String, String]] =
    Right(InMemoryH2(dbConfigKey, port))
}

case class PostGreSQLPersistenceConfig(dbconf: DBConfig)
    extends PersistenceConfig[String, String] {
  override def persistence: ModuleOr[Persistence[String, String]] =
    Right(PostgreSQLPersistence(dbconf))
}
