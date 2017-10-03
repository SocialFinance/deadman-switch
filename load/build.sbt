name := "deadman-switch-load"

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpmime" % "4.5.1",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.1"
)

mainClass in Compile := Some("org.sofi.deadman.load.Main")
