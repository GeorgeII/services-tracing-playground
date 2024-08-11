package com.github.georgeii.zioservice

import zio._
import zio.http._

object MainApp extends ZIOAppDefault {
  private def textRoute(counterRef: Ref[Int]): Route[Any, Nothing] =
    Method.GET / "text" -> handler(
      for {
        counter <- counterRef.updateAndGet(_ + 1)
      } yield Response.text(s"Zio says hi! $counter")
    )

  val jsonRoute: Route[Any, Nothing] =
    Method.GET / "json" -> handler(Response.json("""{"greetings": "Hello World!"}"""))

  // Create HTTP route
  private def routes(counterRef: Ref[Int]): Routes[Any, Nothing] = {
    val composedMiddlewares =
      Middleware.requestLogging(
        loggedRequestHeaders = Set(Header.Accept),
        loggedResponseHeaders = Set(Header.Accept)
      ) @@ Middleware.debug

    Routes(textRoute(counterRef), jsonRoute) @@ composedMiddlewares
  }

  // Run it like any simple app
  override val run: ZIO[Any, Throwable, Nothing] = {
    val serverCounterRefZio = Ref.make(0)
    val clientCounterRefZio = Ref.make(0)

    for {
      clientCounterRef <- clientCounterRefZio
      serverCounterRef <- serverCounterRefZio

      _ <- ZIO.logInfo("bla-bla-bla")
      requestSpammer <- RequestSpammer.make

      nothingClient <-
        requestSpammer.run
          .retry(Schedule.recurs(5) && Schedule.spaced(5.seconds))
          .provide(ZClient.default, ZLayer.succeed(clientCounterRef))
      nothingServer = Server.serve(routes(serverCounterRef)).provide(Server.default)
      raced <- ZIO.raceAll(nothingClient, List(nothingServer))
    } yield raced

//    serverCounterRefZio.flatMap { counterRef =>
//      Server.serve(routes(counterRef)).provide(Server.default)
//    }
  }
}
