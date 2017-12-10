lazy val commonSettings = Seq(
  organization := "com.lambdaminute",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := Versions.scalaVersion
)

scalaVersion in ThisBuild := Versions.scalaVersion

lazy val modelJVM = model.jvm
lazy val modelJS  = model.js

lazy val codegen = (project in file("codegen"))
  .settings(
    libraryDependencies +=
      "com.typesafe.slick" %% "slick-codegen" % "3.2.1")

lazy val server = (project in file("server"))
  .settings(
    commonSettings
  )
  .dependsOn(modelJVM, codegen)

lazy val model =
  (crossProject in file("model"))
    .jvmSettings(
      commonSettings
    )
    .jsSettings(commonSettings)

lazy val client = (project in file("client"))
  .settings(
    commonSettings
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(modelJS)

lazy val wishr =
  (project in file("."))
    .aggregate(server, client)
