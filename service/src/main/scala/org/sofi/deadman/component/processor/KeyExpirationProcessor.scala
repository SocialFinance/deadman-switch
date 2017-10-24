package org.sofi.deadman.component.processor

import akka.actor._
import org.sofi.deadman.messages.event._

final class KeyExpirationProcessor(val id: String, val eventLog: ActorRef, val targetEventLog: ActorRef) extends EventProcessor {
  def processEvent = {
    case TaskExpiration(task, ts) ⇒
      windows.map(window ⇒ KeyExpiration(task, window, ts)).toIndexedSeq
  }
}

object KeyExpirationProcessor {
  def name(id: String): String = s"$id-key-expiration-processor"
  def props(id: String, eventLog: ActorRef, targetEventLog: ActorRef): Props =
    Props(new KeyExpirationProcessor(id, eventLog, targetEventLog))
}
