package org.sofi.deadman.component.counter

import akka.actor._
import org.sofi.deadman.messages.event._

final class KeyCounter(val id: String, val eventLog: ActorRef) extends EventCounter {
  def taskKey(t: Task) = t.key
  def taskTerminationKey(t: TaskTermination) = t.key
}

object KeyCounter {
  def name(id: String): String = s"$id-key-counter"
  def props(id: String, eventLog: ActorRef): Props = Props(new KeyCounter(id, eventLog))
}
