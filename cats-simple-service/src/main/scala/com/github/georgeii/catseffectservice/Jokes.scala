package com.github.georgeii.catseffectservice

import cats.effect.{Concurrent, Ref}
import cats.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.circe._

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

  def impl[F[_]: Concurrent](counterRef: Ref[F, Int]): Jokes[F] = new Jokes[F] {

    def get: F[Jokes.Joke] = {
      for {
        counter <- counterRef.updateAndGet(_ + 1)
      } yield Joke(s"Cats are playing with effects. What a joke! $counter")
    }
  }
}
