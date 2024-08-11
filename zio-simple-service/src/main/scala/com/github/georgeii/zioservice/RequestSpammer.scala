package com.github.georgeii.zioservice

import zio.{Duration, Random, Ref, RIO, Task, ZIO}
import zio.http.{Client, Request, URL}

import scala.concurrent.duration.DurationInt

trait RequestSpammer {

  def run: RIO[Client with RequestSpammer.RequestCounter, Nothing]

}

object RequestSpammer {

  private type RequestCounter = Ref[Int]

  def make: Task[RequestSpammer] = {
    ZIO.attempt {
      new RequestSpammer {
        override def run: RIO[Client with RequestCounter, Nothing] = {
          val maybeUrlsToRequest = ZIO.foreach(
            Vector(
              "http://cats-simple-service:8080/joke",
//              "http://web:5000/hello",
            )
          )(urlString => ZIO.fromEither(URL.decode(urlString)))

          ZIO.scoped {
            ZIO.serviceWithZIO[Client] { client =>
              ZIO.serviceWithZIO[RequestCounter] { counterRef =>
                (for {
                  _ <- ZIO.logInfo("chipi-chipi-chapa-chapa")
                  //              client <- ZIO.service[Client]
//              counterRef <- ZIO.service[RequestCounter]
                  urlsToRequest <- maybeUrlsToRequest
                  _ <- ZIO.logInfo("zxc")
                  randomInt <- Random.nextIntBounded(1000)
                  _ <- ZIO.logInfo("vbn")
                  randomUrl = urlsToRequest(randomInt % urlsToRequest.size)
                  _ <- ZIO.logInfo("vbn")
                  counter <- counterRef.updateAndGet(_ + 1)
                  _ <- ZIO.logInfo("ytr")

//                  res <- client.request(Request.get("https://google.com"))
//                  bodySt <- res.body.asString
//                  _ <- ZIO.logInfo(s"Printing $counter request.") *> ZIO.logInfo(bodySt)

                  _ <- ZIO.logInfo(randomUrl.toString)
                  resp <- client.request(Request.get(randomUrl))
                  bodyStr <- resp.body.asString
                  _ <- ZIO.logInfo(s"Printing $counter request.") *> ZIO.logInfo(bodyStr)
                  _ <- ZIO.sleep(Duration.fromScala(randomInt.millis))
                } yield ()).forever
              }
            }
          }
        }
      }
    }
  }

}
