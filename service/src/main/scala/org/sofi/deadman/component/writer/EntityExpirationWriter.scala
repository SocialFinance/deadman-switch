package org.sofi.deadman.component.writer

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._

final class EntityExpirationWriter(val id: String, val eventLog: ActorRef) extends TaskWriter[EntityExpiration] {

  // Writer ID
  val writerId = "EntityExpirationWriter"

  // Entity query for task expiration
  override def onCommand = {
    case q: GetExpirations ⇒
      val _ = EntityExpiration.select(q.entity.getOrElse("")).map { result ⇒
        Tasks(result.map(_.asTask))
      } recoverWith noTasks pipeTo sender()
  }

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaskExpiration(t, exp) ⇒
      batch(EntityExpiration(t.entity, t.key, t.aggregate, t.ttl, t.ts, exp, t.tags.sorted.mkString(",")))
  }

  // Save an entity expiration to C*
  override def write(model: EntityExpiration) = model.save
}

object EntityExpirationWriter {
  def name(id: String): String = s"$id-ent-expiration-writer"
  def props(id: String, eventLog: ActorRef): Props = Props(new EntityExpirationWriter(id, eventLog))
}
