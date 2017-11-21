name := "deadman-switch-ui"

libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "scalapb-json4s"     % "0.3.3",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test,
  guice,
  ws % Test
)
