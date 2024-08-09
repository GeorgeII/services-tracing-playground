package com.github.georgeii.zioservice

import zio.{Scope, ZIO, ZLayer}
import zio.http._
import zio.http.netty.NettyConfig
import zio.http.netty.server.NettyDriver
import zio.test._

object MainAppSpec extends ZIOSpecDefault {

  private def getHelloWorldRequest: ZIO[Server, Nothing, Request] =
    for {
      portZio <- ZIO.serviceWith[Server](_.port)
      port <- portZio
    } yield Request.get(
      url = URL(Path.root / "json").port(port)
    )

  def spec = suite("suite for MainApp") {

    test("test for endpoint /json") {
      for {
        client <- ZIO.service[Client]
        testRequest <- getHelloWorldRequest
        _ <- TestServer.addRoute(MainApp.jsonRoute)
        response <- client.request(testRequest)
        responseBody <- response.body.asString
      } yield assertTrue(
        response.status == Status.Ok,
        responseBody == """{"greetings": "Hello World!"}""",
      )
    }.provideSome[Client with Driver](TestServer.layer, Scope.default)

  }.provide(
    ZLayer.succeed(Server.Config.default.onAnyOpenPort),
    Client.default,
    NettyDriver.customized,
    ZLayer.succeed(NettyConfig.defaultWithFastShutdown),
  )

}
