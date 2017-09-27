package org.sofi.deadman.component.query

import akka.actor._
import org.sofi.deadman.messages.query._

final class QueryManager(val viewManager: ActorRef, val writerManager: ActorRef) extends Actor {
  def receive: Receive = {
    // In-memory queries
    case query: GetTasks ⇒ viewManager forward query
    // Persistent queries
    case query: GetExpirations ⇒ writerManager forward query
    case query: GetWarnings ⇒ writerManager forward query
    case query: GetTags ⇒ writerManager forward query
  }
}

object QueryManager {
  def props(viewManager: ActorRef, writerManager: ActorRef): Props = Props(new QueryManager(viewManager, writerManager))
}
