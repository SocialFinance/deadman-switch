name := "deadman-switch-domain"

publishArtifact := true
publishMavenStyle := true

libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"
)

PB.targets in Compile := Seq(
  PB.gens.java -> (sourceManaged in Compile).value,
  scalapb.gen(javaConversions=true, grpc=false) -> (sourceManaged in Compile).value
)
