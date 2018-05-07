import sbt.Keys.libraryDependencies

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

val circeVersion  = "0.9.0-M2"
val slinkyVersion = "0.4.2"

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSBundlerPlugin, ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    (npmDependencies in Compile) ++= Seq(
      "react"       -> "16.3.2",
      "react-dom"   -> "16.3.2",
      "react-proxy" -> "1.1.8"
    ),
    (npmDevDependencies in Compile) ++= Seq(
      "file-loader"         -> "1.1.11",
      "style-loader"        -> "0.20.3",
      "css-loader"          -> "0.28.11",
      "html-webpack-plugin" -> "3.2.0",
      "copy-webpack-plugin" -> "4.5.1",
    )
  )
  .settings(
    libraryDependencies ++= Seq("com.lihaoyi" %%% "autowire"  % autoWireVersion,
                                "com.lihaoyi" %%% "scalatags" % scalaTagsVersion),
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser"
    ).map(_ % circeVersion),
    libraryDependencies ++= Seq(
      "me.shadaj" %%% "slinky-core",
      "me.shadaj" %%% "slinky-web",
      "me.shadaj" %%% "slinky-hot",
      "me.shadaj" %%% "slinky-scalajsreact-interop"
    ).map(_ % slinkyVersion),
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack-fastopt.config.js"),
    webpackConfigFile in fullOptJS := Some(baseDirectory.value / "webpack-opt.config.js"),
    webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot"),
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
  )
  .settings(
    version in webpack := "4.5.0",
    version in startWebpackDevServer := "3.1.3"
  )
  .settings(
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSUseMainModuleInitializer := true,
    addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M11" cross CrossVersion.full)
  )
  .dependsOn(modelJS)
