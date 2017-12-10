val commonSettings = (
  scalaVersion := "2.12.3"
)

resolvers += Resolver.sonatypeRepo("snapshots")

val http4sVersion = "0.18.0-M6"

val circeVersion = "0.7.0"
val slickVersion = "3.2.1"
val cirisVersion = "0.4.1"

scalacOptions ++= Seq("-feature", "-language:higherKinds", "-deprecation")

lazy val codegen = project
  .settings(commonSettings)
  .settings(
    libraryDependencies +=
      "com.typesafe.slick" %% "slick-codegen" % slickVersion)

lazy val server = (project in file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test
  )
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s"         %% "http4s-blaze-server" % http4sVersion,
      "org.http4s"         %% "http4s-dsl"          % http4sVersion,
      "org.http4s"         %% "http4s-argonaut"     % http4sVersion,
      "org.http4s"         %% "http4s-circe"        % http4sVersion,
      "com.typesafe.slick" %% "slick"               % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp"      % slickVersion,
      "is.cir"             %% "ciris-core"          % cirisVersion,
      "com.chuusai"        %% "shapeless"           % "2.3.2",
      "com.github.t3hnar"  %% "scala-bcrypt"        % "3.1"
    ),
//Java dependencies
    libraryDependencies ++= Seq(
      "org.slf4j"          % "slf4j-nop"     % "1.6.4",
      "com.h2database"     % "h2"            % "1.4.193",
      "org.postgresql"     % "postgresql"    % "42.1.4",
      "org.apache.commons" % "commons-email" % "1.4",
      "org.flywaydb"       % "flyway-core"   % "4.2.0"
    )
  )
  .dependsOn(codegen)

lazy val generateSlickCode = taskKey[Unit]("Generates the schema classes for Slick DB Access")

generateSlickCode := {
  val runr = (runner in Compile).value
  val cp   = (dependencyClasspath in Compile).value
  val s    = streams.value
  runr.run(
    "flywaycodegen.FlywayCodeGen",
    cp.files,
    Array(
      "slick.jdbc.PostgresProfile",
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost/postgres",
      "src/main/scala/",
      "com.lambdaminute.wishr.model",
      "postgres",
      "pass"
    ),
    s.log
  )
}
