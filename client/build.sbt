name := "deadman-switch-client"

resolvers += "Eventuate Releases" at "https://dl.bintray.com/rbmhtechnology/maven"

libraryDependencies ++= Seq(
  "com.typesafe.akka"  %% "akka-http"                % "10.0.10",
  "com.typesafe.akka"  %% "akka-http-spray-json"     % "10.0.10",
  "com.rbmhtechnology" %% "eventuate-core"           % "0.8.1",
  "com.rbmhtechnology" %% "eventuate-log-leveldb"    % "0.8.1",
  "com.rbmhtechnology" %% "eventuate-adapter-stream" % "0.8.1",
  "com.typesafe.akka"  %% "akka-slf4j"               % "2.4.18",
  "ch.qos.logback"     %  "logback-classic"          % "1.1.6",
  "com.typesafe.akka"  %% "akka-testkit"             % "2.4.12" % "test",
  "org.scalatest"      %% "scalatest"                % "3.0.0"  % "test"
)

fork in Test := true
fork in run := true

javacOptions in (Compile, compile) ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "-Xlint:-options"
)
