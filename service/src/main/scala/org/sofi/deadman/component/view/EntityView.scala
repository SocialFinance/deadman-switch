package org.sofi.deadman.component.view

import akka.actor._
import org.sofi.deadman.messages.event._

final class EntityView(val id: String, val eventLog: ActorRef) extends QueryView {
  override def taskKey(t: Task): String = t.entity
  override def taskTerminationKey(t: TaskTermination): String = t.entity
}

object EntityView {
  def props(id: String, eventLog: ActorRef): Props = Props(new EntityView(id, eventLog))
}
