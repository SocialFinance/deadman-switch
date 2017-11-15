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
    .dependsOn(domain, core, service, client, stream, load)
    .aggregate(domain, core, service, client, stream, load)
    .disablePlugins(RevolverPlugin, JavaAppPackaging, ProtocPlugin)

lazy val domain = project
  .settings(baseSettings: _*)
  .settings(
    publish in Docker := {},
    publishLocal in Docker := {})
  .enablePlugins(SbtNativePackager, ProtocPlugin)
  .disablePlugins(RevolverPlugin)

lazy val core = project
  .dependsOn(domain)
  .settings(baseSettings: _*)
  .settings(
    publish in Docker := {},
    publishLocal in Docker := {})
  .enablePlugins(SbtNativePackager)
  .disablePlugins(ProtocPlugin, RevolverPlugin)

lazy val service = project
  .dependsOn(core)
  .settings(baseSettings: _*)
  .settings(
    mappings in (Compile, packageBin) ++= (mappings in (domain, Compile, packageBin)).value,
    publish := {},
    publishLocal := {},
    publishArtifact := true)
  .enablePlugins(JavaAppPackaging, RevolverPlugin)
  .disablePlugins(ProtocPlugin)

lazy val client = project
  .dependsOn(domain)
  .settings(baseSettings: _*)
  .settings(
    publish in Docker := {},
    publishLocal in Docker := {})
  .enablePlugins(SbtNativePackager)
  .disablePlugins(ProtocPlugin, RevolverPlugin)

lazy val stream = project
  .dependsOn(domain)
  .settings(baseSettings: _*)
  .settings(
    publish in Docker := {},
    publishLocal in Docker := {})
  .enablePlugins(SbtNativePackager)
  .disablePlugins(ProtocPlugin, RevolverPlugin)

lazy val load = project
  .dependsOn(client)
  .settings(baseSettings: _*)
  .settings(
    publishLocal := {},
    publishArtifact := false,
    publish in Docker := {},
    publishLocal in Docker := {})
  .enablePlugins(SbtNativePackager)
  .disablePlugins(ProtocPlugin, RevolverPlugin)

val preferences = ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(RewriteArrowSymbols, true)
  .setPreference(DanglingCloseParenthesis, Preserve)
Seq(preferences)
