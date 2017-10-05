package org.sofi.deadman.component.counter

import akka.actor._
import org.sofi.deadman.messages.event._

final class AggregateCounter(val id: String, val eventLog: ActorRef) extends EventCounter {
  def taskKey(t: Task) = t.aggregate
  def taskTerminationKey(t: TaskTermination) = t.aggregate
}

object AggregateCounter {
  def name(id: String): String = s"$id-aggregate-counter"
  def props(id: String, eventLog: ActorRef): Props = Props(new AggregateCounter(id, eventLog))
}
