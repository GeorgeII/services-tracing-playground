import Dependencies.{Cats, Zio}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

ThisBuild / scalacOptions     := optionsOnOrElse("2.13", "2.12")("-Ywarn-unused")("").value
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalafixDependencies ++= List("com.github.liancheng" %% "organize-imports" % "0.6.0")


lazy val catsSimpleService = project
  .in(file("cats-simple-service"))
  .settings(
    name := "cats-simple-service",
    version := "0.0.1",
    Compile / run / mainClass := Option("com.github.georgeii.catseffectservice.Main"),
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= Cats.all,
  )
  .enablePlugins(JavaAppPackaging)
  .settings(settingsDocker)

lazy val zioSimpleService = project
  .in(file("zio-simple-service"))
  .settings(
    name := "zio-simple-service",
    version := "0.0.1",
    Compile / run / mainClass := Option("com.github.georgeii.zioservice.MainApp"),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    libraryDependencies ++= Zio.all,
  )
  .enablePlugins(JavaAppPackaging)
  .settings(settingsDocker)

lazy val all = (project in file("."))
  .settings(
    name := "services-tracing-playground"
  )
  .aggregate(catsSimpleService, zioSimpleService)

def settingsDocker = Seq(
  Docker / version   := version.value,
  dockerBaseImage    := "eclipse-temurin:20.0.1_9-jre",
)
