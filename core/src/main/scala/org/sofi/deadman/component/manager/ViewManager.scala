package org.sofi.deadman.component.manager

import akka.actor._
import org.sofi.deadman.component.view._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.messages.query.QueryType._

final class ViewManager(val id: String, val eventLog: ActorRef) extends Actor with ActorLogging {

  // Query scheduled tasks by aggregate
  private val aggregateView = context.actorOf(AggregateView.props(AggregateView.name(id), eventLog))

  // Query scheduled tasks by entity
  private val entityView = context.actorOf(EntityView.props(EntityView.name(id), eventLog))

  // Query scheduled tasks by key
  private val keyView = context.actorOf(KeyView.props(KeyView.name(id), eventLog))

  // Forward queries to views
  def receive: Receive = {
    case query: GetTasks ⇒ query.queryType match {
      case AGGREGATE ⇒ aggregateView forward query
      case ENTITY ⇒ entityView forward query
      case KEY ⇒ keyView forward query
      case _ ⇒ sender() ! Tasks(Seq.empty)
    }
  }
}

object ViewManager {
  def props(id: String, eventLog: ActorRef): Props = Props(new ViewManager(id, eventLog))
}
