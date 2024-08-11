package com.github.georgeii.catseffectservice

import cats.effect.{IO, IOApp, Ref}
import cats.effect.std.Random
import org.http4s.ember.client.EmberClientBuilder

import scala.concurrent.duration.{DurationInt, FiniteDuration}

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

          clientForeverRunning =
            retryN(
              CatsEffectServiceClient.run(client, requestSpammerCounterRef),
              n = 5,
              timeout = 5.seconds)
          serverForeverRunning = CatsEffectServiceServer.run[IO](jokeCounterRef)

          // if client of server fails, the application fails
          _ <- IO.race(clientForeverRunning, serverForeverRunning)
        } yield ()
      }
    } >> IO.never
  }

  private def retryN[A](io: IO[A], n: Int, timeout: FiniteDuration): IO[A] = {
    io.attempt.flatMap {
      case Right(aa) => IO.delay(aa)
      case Left(ex) if n > 0 => IO.println(ex) >> IO.sleep(timeout) >> retryN(io, n - 1, timeout)
      case Left(ex) => IO.raiseError(ex)
    }
  }
}
