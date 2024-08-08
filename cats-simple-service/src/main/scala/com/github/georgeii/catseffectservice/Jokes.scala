package com.github.georgeii.catseffectservice

import cats.effect.{Concurrent, Ref}
import cats.effect.kernel.Sync
import cats.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._
import org.http4s.Method._


import scala.concurrent.duration.DurationInt

trait Jokes[F[_]]{
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

  def impl[F[_]: Concurrent](C: Client[F], counterRef: Ref[F, Int]): Jokes[F] = new Jokes[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._


    def get: F[Jokes.Joke] = {
      for {
        counter <- counterRef.updateAndGet(_ + 1)
      } yield Joke(s"Cats are playing with effects. What a joke! $counter")
    }
  }
}
