package org.sofi.deadman.component.writer.expiration

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.component.writer.TaskWriter
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._

final class KeyExpirationWriter(val id: String, val eventLog: ActorRef) extends TaskWriter[KeyExpiration] {

  // Writer ID
  val writerId = "KeyExpirationWriter"

  // Query C* for expirations by key and time window
  def onCommand = {
    case q: GetByKey ⇒
      val start = q.start.getOrElse(Long.MinValue)
      val end = q.end.getOrElse(Long.MaxValue)
      val _ = KeyExpiration.select(q.key, q.window, start, end) map { result ⇒
        Tasks(result.map(_.asTask))
      } recoverWith noTasks pipeTo sender()
  }

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case org.sofi.deadman.messages.event.KeyExpiration(task, window, ts) ⇒
      batch(KeyExpiration(task.key, window, ts, task.aggregate, task.entity, task.ttl, task.ts, task.tags.sorted.mkString(",")))
  }

  // Save a tagged expiration to C*
  override def write(models: Vector[KeyExpiration]) = KeyExpiration.save(models.toList)
}

object KeyExpirationWriter {
  def name(id: String): String = s"$id-key-expiration-writer"
  def props(id: String, eventLog: ActorRef): Props = Props(new KeyExpirationWriter(id, eventLog)).withDispatcher("dispatchers.writer")
}
