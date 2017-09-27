package org.sofi.deadman.component.view

import akka.actor._
import org.sofi.deadman.messages.query._, GetTasks.ViewType._

final class ViewManager(aggregateView: ActorRef, entityView: ActorRef, keyView: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {
    case query: GetTasks ⇒ query.view match {
      case AGGREGATE ⇒ aggregateView forward query
      case ENTITY ⇒ entityView forward query
      case KEY ⇒ keyView forward query
      case _ ⇒ sender() ! Tasks(Seq.empty)
    }
  }
}

object ViewManager {
  def props(aggregateView: ActorRef, entityView: ActorRef, keyView: ActorRef): Props = Props(
    new ViewManager(aggregateView, entityView, keyView)
  )
}
