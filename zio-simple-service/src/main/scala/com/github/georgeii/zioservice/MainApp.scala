package com.github.georgeii.zioservice

import zio._
import zio.http._

import scala.concurrent.duration.DurationInt
import scala.util.{Random => Rnd}

object MainApp extends ZIOAppDefault {

  private val urlsZio = ZIO.foreach(
    Vector(
      "http://cats-simple-service:8080/joke",
    )
  )(url => ZIO.fromEither(URL.decode(url)))

  private def maybeMakeSideRequest: URIO[Client, Unit] = {
    ZIO.scoped {
      (for {
        client <- ZIO.service[Client]

        urls <- urlsZio
        randomInt <- ZIO.attempt(Rnd.nextInt(1000))
        url = urls(randomInt % urls.size)

        shouldResend = randomInt > 300
        _ <- ZIO.when(shouldResend)(
          ZIO.logInfo("Sending request to another service") *>
            ZIO.sleep(Duration.fromScala(DurationInt(randomInt).millis)) *>
            client.request(Request.get(url))
        )
      } yield ()).catchAll { error =>
        ZIO.logErrorCause("Could not send intermediate request", Cause.fail(error))
      }
    }
  }

  private def textRoute(counterRef: Ref[Int]): Route[Client, Nothing] =
    Method.GET / "text" -> handler(
      for {
        counter <- counterRef.updateAndGet(_ + 1)
        _ <- maybeMakeSideRequest
      } yield Response.text(s"Zio says hi! $counter")
    )

  val jsonRoute: Route[Any, Nothing] =
    Method.GET / "json" -> handler(Response.json("""{"greetings": "Hello World!"}"""))

  // Create HTTP route
  private def routes(counterRef: Ref[Int]): Routes[Client, Nothing] = {
    val composedMiddlewares =
      Middleware.requestLogging(
        loggedRequestHeaders = Set(Header.Accept),
        loggedResponseHeaders = Set(Header.Accept)
      ) @@ Middleware.debug

    Routes(textRoute(counterRef), jsonRoute) @@ composedMiddlewares
  }

  // Run it like any simple app
  override val run: ZIO[Any, Throwable, Nothing] = {
    val counterRefZio = Ref.make(0)

    counterRefZio.flatMap { counterRef =>
      Server.serve(routes(counterRef)).provide(Server.default, Client.default)
    }
  }
}
