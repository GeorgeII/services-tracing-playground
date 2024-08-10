package com.github.georgeii.catseffectservice

import cats.effect.{IO, IOApp, Ref}
import cats.effect.std.Random
import org.http4s.ember.client.EmberClientBuilder

object Main extends IOApp.Simple {
  private val jokeCounterRefF: IO[Ref[IO, Int]] = Ref.of(0)
  private val requestSpammerCounterRefF: IO[Ref[IO, Int]] = Ref.of(0)

  val run: IO[Nothing] = {
    val random = Random.scalaUtilRandom[IO]

    EmberClientBuilder.default[IO].build.use { client =>
      random.flatMap { implicit rnd =>
        for {
          jokeCounterRef <- jokeCounterRefF
          requestSpammerCounterRef <- requestSpammerCounterRefF

          clientForeverRunning = CatsEffectServiceClient.run(client, requestSpammerCounterRef)
          serverForeverRunning = CatsEffectServiceServer.run[IO](jokeCounterRef)

          // if client of server fails, the application fails
          _ <- IO.race(clientForeverRunning, serverForeverRunning)
        } yield ()
      }
    } >> IO.never

  }
}
