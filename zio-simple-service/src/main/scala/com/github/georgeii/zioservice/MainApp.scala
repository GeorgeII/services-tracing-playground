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

  private val jsonRoute =
    Method.GET / "json" -> handler(Response.json("""{"greetings": "Hello World!"}"""))

  // Create HTTP route
  private def app(counterRef: Ref[Int]): HttpApp[Any] = {
    val composedMiddlewares = Middleware.requestLogging() @@ Middleware.debug

    Routes(textRoute(counterRef), jsonRoute).toHttpApp @@ composedMiddlewares
  }

  // Run it like any simple app
  override val run: ZIO[Any, Throwable, Nothing] = {
    val counterRefZio = Ref.make(0)

    counterRefZio.flatMap { counterRef =>
      Server.serve(app(counterRef)).provide(Server.default)
    }
  }
}
