package com.github.georgeii.catseffectservice

import cats.effect.{Concurrent, Ref}
import cats.effect.kernel.Async
import cats.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.middleware.Logger

import scala.concurrent.duration.DurationInt
import scala.util.Random

trait Jokes[F[_]] {
  def get: F[Jokes.Joke]
}

object Jokes {
  def apply[F[_]](implicit ev: Jokes[F]): Jokes[F] = ev

  final case class Joke(joke: String) extends AnyVal
  object Joke {
    implicit val jokeDecoder: Decoder[Joke] = deriveDecoder[Joke]
    implicit def jokeEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Joke] =
      jsonOf
    implicit val jokeEncoder: Encoder[Joke] = deriveEncoder[Joke]
    implicit def jokeEntityEncoder[F[_]]: EntityEncoder[F, Joke] =
      jsonEncoderOf
  }

  def impl[F[_]: Async](C: Client[F], counterRef: Ref[F, Int]): Jokes[F] = new Jokes[F] {

    def get: F[Jokes.Joke] = {
      for {
        counter <- counterRef.updateAndGet(_ + 1)
        _ <- maybeMakeSideRequest
      } yield Joke(s"Cats are playing with effects. What a joke! $counter")
    }

    val urls: Vector[Uri] = Vector(
      Uri.unsafeFromString("http://zio-simple-service:8080/text"),
    )

    private def maybeMakeSideRequest: F[Unit] = {
      for {
        randomInt <- Async[F].delay(Random.nextInt(1000))
        url = urls(randomInt % urls.size)

        shouldResend = randomInt > 300
        _ <- Async[F].whenA(shouldResend)(
          Async[F].delay(println("Sending request to another service")) >>
            Async[F].sleep(randomInt.millis) >>
            C.get(url)(response => logRequestOrResponse(response))
        )
      } yield ()

    }

    private def logRequestOrResponse(message: Message[F]): F[Unit] = {
      Logger.logMessage(message)(
        logHeaders = true,
        logBody = true,
      )(str => Async[F].delay(println(str)))
    }

  }
}
