import sbt._

object Dependencies {

  object Cats {

    object version {
      val Http4s = "1.0.0-M38"
      val Circe = "0.14.3"
      val Munit = "0.7.29"
      val Logback = "1.4.14"
      val MunitCatsEffect = "1.0.7"
      val CatsEffect = "3.4.4"
    }

    lazy val http4s: Seq[ModuleID] = Seq(
      "org.http4s"      %% "http4s-ember-server" % version.Http4s,
      "org.http4s"      %% "http4s-ember-client" % version.Http4s,
      "org.http4s"      %% "http4s-circe"        % version.Http4s,
      "org.http4s"      %% "http4s-dsl"          % version.Http4s,
    )
    lazy val circe: Seq[ModuleID] = Seq(
      "io.circe"        %% "circe-generic"       % version.Circe,
    )
    lazy val munit: Seq[ModuleID] = Seq(
      "org.scalameta"   %% "munit"               % version.Munit           % Test,
      "org.typelevel"   %% "munit-cats-effect-3" % version.MunitCatsEffect % Test,
    )
    lazy val logback: Seq[ModuleID] = Seq(
      "ch.qos.logback"  %  "logback-classic"     % version.Logback         % Runtime,
    )
    lazy val catsEffect: Seq[ModuleID] = Seq(
      "org.typelevel" %% "cats-effect" % version.CatsEffect,
    )

    lazy val all: Seq[ModuleID] = Seq(
      Cats.http4s,
      Cats.circe,
      Cats.munit,
      Cats.logback,
      Cats.catsEffect,
    ).flatten
  }

  object Zio {

    object version {
      val zioCore = "2.0.13"
      val zioHttp = "3.0.0-RC9"
    }

    lazy val http: Seq[ModuleID] = Seq(
      "dev.zio" %% "zio-http"     % version.zioHttp,
    )
    lazy val test: Seq[ModuleID] = Seq(
      "dev.zio" %% "zio-test"          % version.zioCore % Test,
      "dev.zio" %% "zio-test-sbt"      % version.zioCore % Test,
      "dev.zio" %% "zio-test-magnolia" % version.zioCore % Test,
      "dev.zio" %% "zio-http-testkit"  % version.zioHttp % Test,
    )

    lazy val all: Seq[ModuleID] = Seq(
      Zio.http,
      Zio.test,
    ).flatten

  }

}
