name := "deadman-switch-client"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"            % "10.0.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10",
  "com.typesafe.akka" %% "akka-slf4j"           % "2.4.18",
  "ch.qos.logback"     % "logback-classic"      % "1.1.6",
  "com.typesafe.akka" %% "akka-testkit"         % "2.4.12" % "test",
  "org.scalatest"     %% "scalatest"            % "3.0.0"  % "test"
)

javacOptions in (Compile, compile) ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "-Xlint:-options"
)
