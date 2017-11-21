name := "deadman-switch-ui"

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test,
  guice,
  ws % Test
)
