enablePlugins(ScalaJSPlugin)

libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.1"

// core = essentials only. No bells or whistles.
//libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "0.11.3"

libraryDependencies ++= Seq(
                             "com.github.japgolly.scalajs-react" %%% "core" % "0.11.3",
                             "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.3"
                           )

libraryDependencies += "com.github.chandu0101.scalajs-react-components" % "core_sjs0.6_2.11" % "0.5.0"

libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.4.3"

libraryDependencies += "org.scala-js" % "scalajs-tools_2.11" % "0.6.14"


// React JS itself (Note the filenames, adjust as needed, eg. to remove addons.)
jsDependencies ++= Seq(

                        "org.webjars.bower" % "react" % "15.3.2"
                        /        "react-with-addons.js"
                        minified "react-with-addons.min.js"
                        commonJSName "React",

                        "org.webjars.bower" % "react" % "15.3.2"
                        /         "react-dom.js"
                        minified  "react-dom.min.js"
                        dependsOn "react-with-addons.js"
                        commonJSName "ReactDOM",

                        "org.webjars.bower" % "react" % "15.3.2"
                        /         "react-dom-server.js"
                        minified  "react-dom-server.min.js"
                        dependsOn "react-dom.js"
                        commonJSName "ReactDOMServer")

skip in packageJSDependencies := false
jsDependencies +=
"org.webjars" % "jquery" % "2.1.4" / "2.1.4/jquery.js"