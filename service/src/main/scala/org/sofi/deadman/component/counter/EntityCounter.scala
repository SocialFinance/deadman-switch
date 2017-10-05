package org.sofi.deadman.component.counter

import akka.actor._
import org.sofi.deadman.messages.event._

final class EntityCounter(val id: String, val eventLog: ActorRef) extends EventCounter {
  def taskKey(t: Task) = t.entity
  def taskTerminationKey(t: TaskTermination) = t.entity
}

object EntityCounter {
  def name(id: String): String = s"$id-entity-counter"
  def props(id: String, eventLog: ActorRef): Props = Props(new EntityCounter(id, eventLog))
}
