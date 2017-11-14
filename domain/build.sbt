name := "deadman-switch-domain"

publishArtifact := true
publishMavenStyle := true

libraryDependencies ++= Seq(
  "org.typelevel"          %% "cats-core"       % "1.0.0-MF",
  "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"
)

PB.targets in Compile := Seq(
  scalapb.gen(singleLineToString=true) -> (sourceManaged in Compile).value
)
