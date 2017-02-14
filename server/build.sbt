scalaVersion := "2.12.1"

val http4sVersion = "0.16.0-cats-SNAPSHOT"

val circeVersion = "0.7.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-argonaut" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "com.typesafe.slick" %% "slick" % "3.2.0-RC1",
  "org.slf4j" % "slf4j-simple" % "1.6.4",
  "com.github.melrief" %% "pureconfig" % "0.5.1"
)


