lazy val commonSettings = Seq(
  organization := "com.lambdaminute",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.11.8"
)


lazy val wishr =
  (project in file("."))
  .aggregate(server, client)

lazy val model = (crossProject in file ("model"))
  .settings( commonSettings )
  .jvmSettings (  )
  .jsSettings ( )

lazy val modelJS = model.js
lazy val modelJVM = model.jvm

lazy val server = (project in file("server"))
  .dependsOn( modelJVM )
  .settings( commonSettings )

lazy val client = (project in file("client"))
  .dependsOn( modelJS )
  .settings( commonSettings )
