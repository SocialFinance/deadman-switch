package org.sofi.deadman.component.view

import akka.actor._
import org.sofi.deadman.messages.query._, GetTasks.ViewType._

final class QueryManager(aggregateView: ActorRef, entityView: ActorRef, keyView: ActorRef, tagView: ActorRef)
  extends Actor with ActorLogging {

  def receive: Receive = {
    case query: GetTasks ⇒ query.view match {
      case AGGREGATE ⇒ aggregateView forward query
      case ENTITY ⇒ entityView forward query
      case KEY ⇒ keyView forward query
      case _ ⇒ sender() ! Tasks(Seq.empty)
    }
    case query: GetTags ⇒ tagView forward query
  }
}

object QueryManager {
  def props(aggregateView: ActorRef, entityView: ActorRef, keyView: ActorRef, tagView: ActorRef): Props =
    Props(new QueryManager(aggregateView, entityView, keyView, tagView))
}
