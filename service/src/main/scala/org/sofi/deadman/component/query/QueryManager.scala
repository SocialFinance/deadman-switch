package org.sofi.deadman.component.query

import akka.actor._
import org.sofi.deadman.messages.query._

final class QueryManager(val viewManager: ActorRef, val writerManager: ActorRef) extends Actor {

  // Forward queries to the appropriate manager
  def receive: Receive = {
    case query: GetTasks ⇒ viewManager forward query
    case query: GetExpirations ⇒ writerManager forward query
    case query: GetWarnings ⇒ writerManager forward query
    case query: GetTags ⇒ writerManager forward query
  }
}

object QueryManager {
  def props(viewManager: ActorRef, writerManager: ActorRef): Props = Props(new QueryManager(viewManager, writerManager))
}
