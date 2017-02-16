package com.lambdaminute.wishr.config

import cats.data.Kleisli
import com.lambdaminute.wishr.config.Module.ModuleOr
import com.lambdaminute.wishr.persistence.{Persistence, WeakPersistence}

import scala.util.Either

class PersistenceModule {

  def fromConfig[Error, Secret]: Kleisli[ModuleOr, PersistenceConfig[Error, Secret], Persistence[Error, Secret]] =
    Kleisli(_.persistence)

}
