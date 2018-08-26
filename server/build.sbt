resolvers += Resolver.sonatypeRepo("snapshots")
import Versions._
val http4sVersion = "0.18.16"

val cirisVersion  = "0.4.1"
val doobieVersion = "0.5.2"

scalacOptions ++= Seq("-feature", "-language:higherKinds", "-deprecation")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test

libraryDependencies ++= Seq(
  "org.http4s"        %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"        %% "http4s-dsl"          % http4sVersion,
  "org.http4s"        %% "http4s-argonaut"     % http4sVersion,
  "org.http4s"        %% "http4s-circe"        % http4sVersion,
  "is.cir"            %% "ciris-core"          % cirisVersion,
  "io.circe"          %% "circe-core"          % circeVersion,
  "io.circe"          %% "circe-generic"       % circeVersion,
  "io.circe"          %% "circe-parser"        % circeVersion,
  "com.chuusai"       %% "shapeless"           % shapelessVersion,
  "com.github.t3hnar" %% "scala-bcrypt"        % "3.1",
  "org.tpolecat"      %% "doobie-core"         % doobieVersion,
  "org.tpolecat"      %% "doobie-h2"           % doobieVersion, // H2 driver 1.4.197 + type mappings.
  "org.tpolecat"      %% "doobie-postgres"     % doobieVersion, // Postgres driver 42.2.2 + type mappings.,
  "org.log4s"         %% "log4s"               % "1.6.1"
)
//Java dependencies
libraryDependencies ++= Seq(
  "com.h2database"     % "h2"              % "1.4.193",
  "org.postgresql"     % "postgresql"      % "42.1.4",
  "org.apache.commons" % "commons-email"   % "1.4",
  "org.flywaydb"       % "flyway-core"     % "4.2.0",
  "ch.qos.logback"     % "logback-classic" % "1.2.3",
)
