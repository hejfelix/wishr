import Versions._

lazy val commonSettings = Seq(
  organization := "com.lambdaminute",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := scalaV
)

addCommandAlias("startClient", ";project client;fastOptJS::startWebpackDevServer;~fastOptJS")
addCommandAlias("startBackend", ";project server;~reStart")

scalaVersion in ThisBuild := scalaV

val autoWireVersion  = "0.2.6"
val scalaTagsVersion = "0.6.7"

lazy val modelJVM = model.jvm
lazy val modelJS  = model.js

lazy val server = (project in file("server"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.lihaoyi"   %% "autowire"    % autoWireVersion,
      "com.lihaoyi"   %% "scalatags"   % scalaTagsVersion,
      "org.typelevel" %% "cats-core"   % cats,
      "org.typelevel" %% "cats-effect" % catsEffect
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
    scalacOptions ++= Seq("-Ypartial-unification")
  )
  .dependsOn(modelJVM)

lazy val model =
  (crossProject in file("model"))
    .jvmSettings(
      commonSettings
    )
    .jsSettings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.chuusai" %%% "shapeless"    % Versions.shapelessVersion,
        "io.circe"    %% "circe-core"    % circeVersion,
        "io.circe"    %% "circe-generic" % circeVersion,
        "io.circe"    %% "circe-parser"  % circeVersion
      )
    )

val circeVersion          = "0.9.0-M2"
val slinkyVersion         = "0.4.2"
val slinkyWrappersVersion = "0.2.0"
val materialUiVersion     = "1.2.0"

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSBundlerPlugin, ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    (npmDependencies in Compile) ++= Seq(
      "react"              -> "16.3.2",
      "react-dom"          -> "16.3.2",
      "react-proxy"        -> "1.1.8",
      "@material-ui/core"  -> materialUiVersion,
      "@material-ui/icons" -> "1.1.0",
      "react-router-dom"   -> "4.2.2"
    ),
    (npmDevDependencies in Compile) ++= Seq(
      "file-loader"         -> "1.1.11",
      "style-loader"        -> "0.20.3",
      "css-loader"          -> "0.28.11",
      "html-webpack-plugin" -> "3.2.0",
      "copy-webpack-plugin" -> "4.5.1",
      "glamor"              -> "2.20.40"
    )
  )
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi"      %%% "autowire"                     % autoWireVersion,
      "com.lihaoyi"      %%% "scalatags"                    % scalaTagsVersion,
      "com.lambdaminute" %%% "slinky-wrappers-material-ui"  % slinkyWrappersVersion,
      "com.lambdaminute" %%% "slinky-wrappers-react-router" % slinkyWrappersVersion,
      "org.scala-js"     %%% "scalajs-dom"                  % "0.9.2"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser"
    ).map(_ % circeVersion),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % cats,
      "org.typelevel" %% "cats-effect" % catsEffect
    ),
    libraryDependencies ++= Seq(
      "me.shadaj" %%% "slinky-core",
      "me.shadaj" %%% "slinky-web",
      "me.shadaj" %%% "slinky-hot",
      "me.shadaj" %%% "slinky-scalajsreact-interop"
    ).map(_ % slinkyVersion),
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack-fastopt.config.js"),
    webpackConfigFile in fullOptJS := Some(baseDirectory.value / "webpack-opt.config.js"),
    webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot", "--host", "0.0.0.0"),
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly()
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
