package org.sofi.deadman.component.view

import akka.actor._
import org.sofi.deadman.messages.event._

final class KeyView(val id: String, val eventLog: ActorRef) extends QueryView {
  override def taskKey(t: Task): String = t.key
  override def taskTerminationKey(t: TaskTermination): String = t.key
}

object KeyView {
  def props(id: String, eventLog: ActorRef): Props = Props(new KeyView(id, eventLog))
}
