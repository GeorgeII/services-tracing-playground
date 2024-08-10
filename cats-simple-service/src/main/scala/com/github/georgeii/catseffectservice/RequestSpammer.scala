package com.github.georgeii.catseffectservice

import cats.effect.{Async, Ref}
import cats.effect.std.{Console, Random}
import cats.implicits._
import org.http4s.{Message, Uri}
import org.http4s.client.Client
import org.http4s.client.middleware.Logger

import scala.concurrent.duration.DurationInt

trait RequestSpammer[F[_]] {

  def sendRequests: F[Nothing]

}

object RequestSpammer {

  def apply[F[_]](implicit ev: RequestSpammer[F]): RequestSpammer[F] = ev

  def impl[F[_]: Async: Random: Console](
      C: Client[F],
      counterRef: Ref[F, Int],
      urlToSendRequestsTo: Vector[Uri]
  ): RequestSpammer[F] = new RequestSpammer[F] {

    override def sendRequests: F[Nothing] = {
      (for {
        randomNumber <- Random[F].nextIntBounded(1000)
        randomUri = urlToSendRequestsTo(randomNumber % urlToSendRequestsTo.size)
        counter <- counterRef.updateAndGet(_ + 1)
        _ <- C.get(randomUri)(response => logRequestOrResponse(response, counter))
        _ <- Async[F].sleep(randomNumber.millis)
      } yield ())
//        .handleErrorWith(Console[F].printStackTrace)
        .foreverM
    }

    private def logRequestOrResponse(message: Message[F], counter: Int): F[Unit] = {
      Logger.logMessage(message)(
        logHeaders = true,
        logBody = true,
      )(str => Console[F].println(s"Printing $counter request.") >> Console[F].println(str))
    }

  }

}
