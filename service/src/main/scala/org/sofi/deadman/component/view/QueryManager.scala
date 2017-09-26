package org.sofi.deadman.component.view

import akka.actor._
import org.sofi.deadman.messages.query._

final class QueryManager(aggregateView: ActorRef, entityView: ActorRef, tagView: ActorRef)
  extends Actor with ActorLogging {

  def receive: Receive = {
    case query: GetTasks ⇒ query.view match {
      case GetTasks.ViewType.AGGREGATE ⇒ aggregateView forward query
      case GetTasks.ViewType.ENTITY ⇒ entityView forward query
      case _ ⇒ sender() ! Tasks(Seq.empty)
    }
    case query: GetTags ⇒ tagView forward query
  }
}

object QueryManager {
  def props(aggregateView: ActorRef, entityView: ActorRef, tagView: ActorRef): Props =
    Props(new QueryManager(aggregateView, entityView, tagView))
}
