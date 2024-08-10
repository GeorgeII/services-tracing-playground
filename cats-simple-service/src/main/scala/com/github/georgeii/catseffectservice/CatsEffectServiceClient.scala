package com.github.georgeii.catseffectservice

import cats.effect.{Async, Ref}
import cats.effect.std.{Console, Random}
import org.http4s.Uri
import org.http4s.client.Client

object CatsEffectServiceClient {

  def run[F[_]: Async: Random: Console](
      client: Client[F],
      requestSpammerCounterRef: Ref[F, Int]
  ): F[Nothing] = {
    val urlsToRequest = Vector(
      Uri.unsafeFromString("http://zio-simple-service:8080/text"),
    )

    RequestSpammer.impl[F](client, requestSpammerCounterRef, urlsToRequest).sendRequests
  }

}
