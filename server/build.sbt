scalaVersion := "2.12.1"

val http4sVersion = "0.16.0-cats-SNAPSHOT"

val circeVersion = "0.7.0"

val doobieVersion = "0.4.1"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % "3.0"

libraryDependencies ++= Seq(
  "org.http4s"         %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"         %% "http4s-dsl"          % http4sVersion,
  "org.http4s"         %% "http4s-argonaut"     % http4sVersion,
  "org.http4s"         %% "http4s-circe"        % http4sVersion,
  "org.slf4j"          % "slf4j-simple"         % "1.6.4",
  "com.github.melrief" %% "pureconfig"          % "0.5.1"
)

//Doobie stuff
libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core-cats",
  "org.tpolecat" %% "doobie-h2-cats",
  "org.tpolecat" %% "doobie-postgres-cats"
).map(_ % doobieVersion)

//Java dependencies
libraryDependencies += "com.h2database" % "h2" % "1.4.193"
libraryDependencies += "org.apache.commons" % "commons-email" % "1.4"

packAutoSettings
