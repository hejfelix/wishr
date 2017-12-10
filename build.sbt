lazy val commonSettings = Seq(
  organization := "com.lambdaminute",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.1"
)

lazy val modelJVM = model.jvm
lazy val modelJS = model.js

lazy val server = (project in file("server"))
  .settings(
    commonSettings
  )
  .dependsOn(modelJVM)

lazy val model =
  (crossProject.crossType(CrossType.Pure) in file("model")).settings(
    commonSettings
  )

lazy val client = (project in file("client"))
  .settings(
    commonSettings
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(modelJS)

lazy val wishr =
  (project in file("."))
    .aggregate(server, client)
