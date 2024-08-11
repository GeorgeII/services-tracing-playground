package com.github.georgeii.requestspammerservice

import cats.effect.{IO, IOApp, Ref}
import org.http4s.Uri
import org.http4s.client.middleware.Logger
import org.http4s.ember.client.EmberClientBuilder

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random

object RequestSpammer extends IOApp.Simple {

  private val urls: Vector[Uri] = Vector(
    Uri.unsafeFromString("http://zio-simple-service:8080/text"),
    Uri.unsafeFromString("http://cats-simple-service:8080/joke"),
  )

  private val counterRefF: IO[Ref[IO, Int]] = Ref.of(0)

  implicit class Retrier[A](io: IO[A]) {
    def retry(n: Int, timeout: FiniteDuration): IO[A] = {
      io.attempt.flatMap {
        case Right(value) => IO(value)
        case Left(err) if n > 0 =>
          IO.println(s"Couldn't send the request. I'll retry $n more times") >>
            IO(err.printStackTrace()) >>
            IO.sleep(timeout) >>
            retry(n - 1, timeout)
        case Left(err) => IO.raiseError(err)
      }
    }
  }

  val run: IO[Unit] = {
    counterRefF.flatMap { counterRef =>
      EmberClientBuilder.default[IO].build.use { client =>
        (for {
          counter <- counterRef.updateAndGet(_ + 1)

          randomInt <- IO(Random.nextInt(1000))
          url = urls(randomInt % urls.size)

          _ <- IO(println(s"Sending request $counter to $url"))
          _ <- client.get(url)(response =>
            Logger.logMessage(response)(
              logHeaders = true,
              logBody = true,
            )(str => IO(println(str))))
          _ <- IO.sleep(randomInt.millis)
        } yield ())
          .foreverM
          .retry(5, 5.seconds)
      }
    }
  }
}
