package org.sofi.deadman.component.writer

import akka.actor._
import org.sofi.deadman.messages.query._

final class WriterManager(val expirationWriter: ActorRef, val warningWriter: ActorRef, val tagWriter: ActorRef) extends Actor {

  // Forward warning and expiration queries to the appropriate writer
  def receive: Receive = {
    case query: GetExpirations ⇒ expirationWriter forward query
    case query: GetWarnings ⇒ warningWriter forward query
    case query: GetTags ⇒ tagWriter forward query
  }
}

object WriterManager {
  def props(expirationWriter: ActorRef, warningWriter: ActorRef, tagWriter: ActorRef): Props = Props(
    new WriterManager(expirationWriter, warningWriter, tagWriter)
  )
}
