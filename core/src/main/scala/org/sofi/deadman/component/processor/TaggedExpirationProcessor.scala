package org.sofi.deadman.component.processor

import akka.actor._
import org.sofi.deadman.log._
import org.sofi.deadman.messages.event._

final class TaggedExpirationProcessor(val id: String, val eventLogs: Map[String, ActorRef]) extends EventProcessor {

  // Process `TaskExpiration` events from this log
  val eventLog = eventLogs(EventLog.name)

  // Persist `TaggedExpiration` events to this log
  val targetEventLog = eventLogs(TagLog.name)

  // Create a tagged expiration set when a task expires
  def processEvent = {
    case TaskExpiration(task, ts) ⇒
      task.tags.flatMap(tag ⇒ windows.map(window ⇒ TaggedExpiration(task, tag, window, ts))).toIndexedSeq
  }
}

object TaggedExpirationProcessor {
  def name(id: String): String = s"$id-tagged-expiration-processor"
  def props(id: String, eventLogs: Map[String, ActorRef]): Props = Props(new TaggedExpirationProcessor(id, eventLogs))
}
