package org.sofi.deadman.http.api

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.sofi.deadman.messages.command._
import scala.concurrent.Future

final class CommandApi(commandManager: ActorRef)(implicit val system: ActorSystem, timeout: Timeout) {
  def snapshot(id: String): Future[CommandResponse] =
    (commandManager ? SaveState(id)).mapTo[CommandResponse]
}
