package org.sofi.deadman.component.view

import akka.actor._
import org.sofi.deadman.messages.event._

final class AggregateView(val id: String, val eventLog: ActorRef) extends QueryView {
  def taskKey(t: Task): String = t.aggregate
  def taskTerminationKey(t: TaskTermination): String = t.aggregate
}

object AggregateView {
  def name(id: String): String = s"$id-aggregate-view"
  def props(id: String, eventLog: ActorRef): Props = Props(new AggregateView(id, eventLog))
}
