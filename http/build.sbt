name := "deadman-switch"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"            % "10.0.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10"
)

fork in run := true
Keys.connectInput in run := true

javacOptions in (Compile, compile) ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "-Xlint:-options"
)

mainClass in Compile := Some("org.sofi.deadman.Main")
