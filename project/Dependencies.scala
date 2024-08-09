import sbt._

object Dependencies {

  object Cats {

    object version {
      val Http4s = "1.0.0-M38"
      val Circe = "0.14.3"
      val Munit = "0.7.29"
      val Logback = "1.4.14"
      val MunitCatsEffect = "1.0.7"
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

    lazy val all: Seq[ModuleID] = Seq(
      Cats.http4s,
      Cats.circe,
      Cats.munit,
      Cats.logback,
    ).flatten
  }

}
