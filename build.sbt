import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

lazy val baseSettings = Seq(
  name := "deadman-switch-root",
  organization := "org.sofi",
  version := "1.0",
  mainClass in Compile := None,
  publishArtifact := false,
  autoScalaLibrary := false,
  crossPaths := false
)

lazy val root =
  project.in(file("."))
    .settings(baseSettings: _*)
    .settings(
      aggregate in run := false,
      aggregate in dist := false,
      aggregate in Docker := false,
      aggregate in publish := false,
      publish := {},
      publishLocal := {},
      publishArtifact := false,
      packagedArtifacts := Map.empty)
    .dependsOn(domain, service, loader)
    .aggregate(domain, service, loader)
    .disablePlugins(RevolverPlugin, JavaAppPackaging, ProtocPlugin)

lazy val domain = project
  .settings(baseSettings: _*)
  .settings(
    publish in Docker := {},
    publishLocal in Docker := {})
  .enablePlugins(SbtNativePackager, ProtocPlugin)
  .disablePlugins(RevolverPlugin)

lazy val service = project
  .dependsOn(domain)
  .settings(baseSettings: _*)
  .settings(
    mappings in (Compile, packageBin) ++= (mappings in (domain, Compile, packageBin)).value,
    publish := {},
    publishLocal := {},
    publishArtifact := true)
  .enablePlugins(JavaAppPackaging, RevolverPlugin)
  .disablePlugins(ProtocPlugin)

lazy val loader = project.in(file("load"))
  .dependsOn(domain)
  .settings(baseSettings: _*)
  .settings(
    publish in Docker := {},
    publishLocal in Docker := {})
  .enablePlugins(SbtNativePackager)
  .disablePlugins(ProtocPlugin, RevolverPlugin)

val preferences = ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(RewriteArrowSymbols, true)
  .setPreference(DanglingCloseParenthesis, Preserve)
Seq(preferences)
