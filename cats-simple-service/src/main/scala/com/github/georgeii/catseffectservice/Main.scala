package com.github.georgeii.catseffectservice

import cats.effect.{IO, IOApp, Ref}

object Main extends IOApp.Simple {
  private val jokeCounterRefF: IO[Ref[IO, Int]] = Ref.of(0)

  val run: IO[Nothing] = jokeCounterRefF.flatMap { jokeCounterRef =>
    CatsEffectServiceServer.run[IO](jokeCounterRef)
  }
}
