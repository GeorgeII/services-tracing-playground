import Dependencies.Cats

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"


lazy val catsSimpleService = project
  .in(file("cats-simple-service"))
  .settings(
    name := "cats-simple-service",
    version := "0.0.1",
    Compile / run / mainClass := Option("com.github.georgeii.catseffectservice.Main"),
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= Cats.all,
  )

lazy val zioSimpleService = project
  .in(file("zio-simple-service"))
  .settings(
    name := "zio-simple-service",
    version := "0.0.1",
  )

lazy val all = (project in file("."))
  .settings(
    name := "services-tracing-playground"
  )
  .aggregate(catsSimpleService, zioSimpleService)
