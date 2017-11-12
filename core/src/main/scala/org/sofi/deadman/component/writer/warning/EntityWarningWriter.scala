package org.sofi.deadman.component.writer.warning

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.component.writer.ModelWriter
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._

final class EntityWarningWriter(val id: String, val eventLog: ActorRef) extends ModelWriter[EntityWarning] {

  // Writer ID
  val writerId = "EntityWarningWriter"

  // Entity query for task warnings
  def onCommand = {
    case q: GetWarnings ⇒
      val _ = EntityWarning.select(q.entity.getOrElse("")).map { result ⇒
        Tasks(result.map(_.asTask))
      } recoverWith noTasksFound pipeTo sender()
  }

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaskWarning(t, ttw, ts) ⇒
      batch(EntityWarning(t.entity, t.key, ttw, t.aggregate, t.ttl, t.ts, ts, t.tags.toSet))
  }

  // Save an entity expiration to C*
  override def write(models: Vector[EntityWarning]) = EntityWarning.save(models.toList)
}

object EntityWarningWriter {
  def name(id: String): String = s"$id-ent-warning-writer"
  def props(id: String, eventLog: ActorRef): Props =
    Props(new EntityWarningWriter(id, eventLog)).withDispatcher("dispatchers.writer")
}
