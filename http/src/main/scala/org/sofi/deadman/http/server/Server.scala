package org.sofi.deadman.http.server

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

trait Server {
  def startup(routes: Route)(implicit system: ActorSystem, am: ActorMaterializer): Unit = {
    implicit val ec = system.dispatcher
    val log = Logging(system.eventStream, "deadman-switch")
    val host = system.settings.config.getString("http.host")
    val port = system.settings.config.getInt("http.port")
    Http().bindAndHandle(routes, host, port).map { binding ⇒
      log.info(s"RestApi bound to ${binding.localAddress}")
    } onFailure {
      case e: Exception ⇒
        log.error(e, "Failed to bind to {}:{}!", host, port)
        system.terminate()
    }
  }
}
