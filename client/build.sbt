val circeVersion = "0.9.0-M2"
libraryDependencies ++= Seq(
  "io.circe" %%% "circe-core",
  "io.circe" %%% "circe-generic",
  "io.circe" %%% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"



//workbenchDefaultRootObject := Some(("client/target/scala-2.12/classes/index-dev.html", "client/target/scala-2.12/"))
