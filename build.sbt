lazy val commonSettings = Seq(
  organization := "com.lambdaminute",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.1"
)


lazy val root = (project in file(".")).aggregate(server, client)

lazy val server = (project in file("server"))
  .settings( commonSettings )

lazy val client = (project in file("client"))
  .dependsOn( server )
  .settings( commonSettings )
