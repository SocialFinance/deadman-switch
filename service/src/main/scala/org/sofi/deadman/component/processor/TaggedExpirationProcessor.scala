package org.sofi.deadman.component.processor

import akka.actor._
import org.sofi.deadman.messages.event._

final class TaggedExpirationProcessor(val id: String, val eventLog: ActorRef, val targetEventLog: ActorRef) extends EventProcessor {

  // Create a tagged expiration set when a task expires
  def processEvent = {
    case TaskExpiration(task, ts) ⇒
      task.tags.flatMap(tag ⇒ windows.map(window ⇒ TaggedExpiration(task, tag, window, ts))).toIndexedSeq
  }
}

object TaggedExpirationProcessor {
  def name(id: String): String = s"$id-tagged-expiration-processor"
  def props(id: String, eventLog: ActorRef, targetEventLog: ActorRef): Props = Props(
    new TaggedExpirationProcessor(id, eventLog, targetEventLog)
  )
}
