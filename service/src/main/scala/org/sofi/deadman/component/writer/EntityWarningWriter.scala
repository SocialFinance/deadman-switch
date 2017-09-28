package org.sofi.deadman.component.writer

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._
import scala.concurrent.Future

final class EntityWarningWriter(val id: String, val eventLog: ActorRef) extends TaskWriter[EntityWarning] {

  // Writer ID
  val writerId = "EntityWarningWriter"

  // Entity query for task warnings
  override def onCommand = {
    case q: GetWarnings ⇒
      val _ = EntityWarning.select(q.entity.getOrElse("")).map { result ⇒
        Tasks(result.map(w ⇒ Task(w.key, w.aggregate, w.entity, w.creation, w.ttl, Seq(w.ttw), w.tags.split(","))))
      } recoverWith noTasks pipeTo sender()
  }

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaskWarning(t, ttw) ⇒
      cache(EntityWarning(t.entity, t.key, ttw, t.aggregate, t.ttl, t.ts, System.currentTimeMillis(), t.tags.sorted.mkString(",")))
  }

  // Save an entity expiration to C*
  override def save(model: EntityWarning): Future[Unit] = model.save
}

object EntityWarningWriter {
  def name(id: String): String = s"$id-ent-warning-writer"
  def props(id: String, eventLog: ActorRef): Props = Props(new EntityWarningWriter(id, eventLog))
}
