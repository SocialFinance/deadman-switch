name := "deadman-switch"

resolvers += "Eventuate Releases" at "https://dl.bintray.com/rbmhtechnology/maven"

libraryDependencies ++= Seq(
  "com.rbmhtechnology" %% "eventuate-core"                     % "0.8.1",
  "com.rbmhtechnology" %% "eventuate-log-cassandra"            % "0.8.1",
  "io.getquill"        %% "quill-cassandra"                    % "1.2.1",
  "com.typesafe.akka"  %% "akka-http"                          % "10.0.8",
  "com.typesafe.akka"  %% "akka-http-spray-json-experimental"  % "2.4.11",
  "com.typesafe.akka"  %% "akka-slf4j"                         % "2.4.18",
  "ch.qos.logback"     %  "logback-classic"                    % "1.1.6",
  "com.rbmhtechnology" %% "eventuate-log-leveldb"              % "0.8.1"   % "test",
  "com.typesafe.akka"  %% "akka-testkit"                       % "2.4.12"  % "test",
  "org.scalatest"      %% "scalatest"                          % "3.0.0"   % "test"
)

fork in Test := true
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
