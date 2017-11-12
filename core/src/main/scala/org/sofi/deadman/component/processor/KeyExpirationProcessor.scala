package org.sofi.deadman.component.processor

import akka.actor._
import org.sofi.deadman.log._
import org.sofi.deadman.messages.event._

final class KeyExpirationProcessor(val id: String, val eventLogs: Map[String, ActorRef]) extends EventProcessor {

  // Process `TaskExpiration` events from this log
  val eventLog = eventLogs(EventLog.name)

  // Persist `KeyExpiration` events to this log
  val targetEventLog = eventLogs(KeyLog.name)

  // Create a tagged expiration set when a task expires
  def processEvent = {
    case TaskExpiration(task, ts) ⇒
      windows.map(window ⇒ KeyExpiration(task, window, ts)).toIndexedSeq
  }
}

object KeyExpirationProcessor {
  def name(id: String): String = s"$id-key-expiration-processor"
  def props(id: String, eventLogs: Map[String, ActorRef]): Props = Props(new KeyExpirationProcessor(id, eventLogs))
}
