package com.github.georgeii.catseffectservice.opentelemetry

import cats.effect.{IO, Resource}
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.{Tracer, TracerProvider}

object TracerInstance {

  def tracer: Resource[IO, Tracer[IO]] =
    OtelJava.autoConfigured[IO]().evalMap(_.tracerProvider.get("cats-simple-service"))

}
