package com.github.georgeii.catseffectservice

import cats.effect.{IO, IOApp, Ref}
import com.github.georgeii.catseffectservice.opentelemetry.TracerInstance.tracer

object Main extends IOApp.Simple {
  private val jokeCounterRefF: IO[Ref[IO, Int]] = Ref.of(0)

  val run: IO[Nothing] = jokeCounterRefF.flatMap { jokeCounterRef =>
    tracer
      .evalMap { implicit tracer =>
        CatsEffectServiceServer.run[IO](jokeCounterRef)
      }
      .useForever
  }
}
