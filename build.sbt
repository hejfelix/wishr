lazy val commonSettings = Seq(
  organization := "com.lambdaminute",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.11.8"
)


lazy val wishr = (project in file(".")).aggregate(server, client)

lazy val server = (project in file("server"))
  .settings( commonSettings )

lazy val client = (project in file("client"))
  .dependsOn( server )
  .settings( commonSettings )
