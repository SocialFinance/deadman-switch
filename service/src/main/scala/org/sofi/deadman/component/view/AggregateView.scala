package org.sofi.deadman.component.view

import akka.actor._
import org.sofi.deadman.messages.event._

final class AggregateView(val id: String, val eventLog: ActorRef) extends QueryView {
  override def taskKey(t: Task): String = t.aggregate
  override def taskTerminationKey(t: TaskTermination): String = t.aggregate
}

object AggregateView {
  def props(id: String, eventLog: ActorRef): Props = Props(new AggregateView(id, eventLog))
}
