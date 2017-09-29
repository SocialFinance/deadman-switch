package org.sofi.deadman.component.writer

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._

final class TaggedExpirationWriter(val id: String, val eventLog: ActorRef) extends TaskWriter[TagExpiration] {

  // Writer ID
  val writerId = "TaggedExpirationWriter"

  // Query C* for tagged expirations
  override def onCommand = {
    case q: GetTags ⇒
      val start = q.start.getOrElse(Long.MinValue)
      val end = q.end.getOrElse(Long.MaxValue)
      val _ = TagExpiration.select(q.tag, q.window, start, end) map { result ⇒
        Tasks(result.map(_.asTask))
      } recoverWith noTasks pipeTo sender()
  }

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaggedExpiration(task, tag, window, ts) ⇒
      batch(TagExpiration(tag, window, ts, task.aggregate, task.entity, task.key, task.ttl, task.ts, task.tags.sorted.mkString(",")))
  }

  // Save a tagged expiration to C*
  override def write(model: TagExpiration) = model.save
}

object TaggedExpirationWriter {
  def name(id: String): String = s"$id-tagged-expiration-writer"
  def props(id: String, eventLog: ActorRef): Props = Props(new TaggedExpirationWriter(id, eventLog))
}
