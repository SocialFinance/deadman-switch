package org.sofi.deadman.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext

trait Server {
  def startup(routes: Route)(implicit system: ActorSystem, ec: ExecutionContext, am: ActorMaterializer): Unit = {
    val host = system.settings.config.getString("http.host")
    val port = system.settings.config.getInt("http.port")
    Http().bindAndHandle(routes, host, port).map { binding ⇒
      system.log.info(s"Bound to ${binding.localAddress}")
    } onFailure {
      case e: Exception ⇒
        system.log.error(e, "Failed to bind to {}:{}!", host, port)
        system.terminate()
    }
  }
}
