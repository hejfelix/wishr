lazy val commonSettings = Seq(
  organization := "com.lambdaminute",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := Versions.scalaVersion
)

scalaVersion in ThisBuild := Versions.scalaVersion

val autoWireVersion  = "0.2.6"
val scalaTagsVersion = "0.6.7"

lazy val modelJVM = model.jvm
lazy val modelJS  = model.js

lazy val codegen = (project in file("codegen"))
  .settings(
    libraryDependencies +=
      "com.typesafe.slick" %% "slick-codegen" % "3.2.1")

lazy val server = (project in file("server"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "autowire"  % autoWireVersion,
      "com.lihaoyi" %% "scalatags" % scalaTagsVersion
    ),
    (resources in Compile) += {
      (fastOptJS in (client, Compile)).value
      (artifactPath in (client, Compile, fastOptJS)).value
    }
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
    commonSettings,
    libraryDependencies ++= Seq("com.lihaoyi" %%% "autowire"  % autoWireVersion,
                                "com.lihaoyi" %%% "scalatags" % scalaTagsVersion),
    scalaJSUseMainModuleInitializer := true
  )
  .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
  .dependsOn(modelJS)
//
//lazy val wishr =
//  (project in file("."))
//    .aggregate(server, client)
