package org.sofi.deadman.component.writer.warning

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.component.writer.TaskWriter
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._

final class AggregateWarningWriter(val id: String, val eventLog: ActorRef) extends TaskWriter[AggregateWarning] {

  // Writer ID
  val writerId = "AggregateWarningWriter"

  // Aggregate query for task warnings
  def onCommand: Receive = {
    case q: GetWarnings ⇒
      val _ = AggregateWarning.select(q.aggregate.getOrElse("")).map { result ⇒
        Tasks(result.map(_.asTask))
      } recoverWith noTasks pipeTo sender()
  }

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaskWarning(t, ttw, ts) ⇒
      batch(AggregateWarning(t.aggregate, t.key, ttw, t.entity, t.ttl, t.ts, ts, t.tags.sorted.mkString(",")))
  }

  // Save an aggregate warning to C*
  override def write(models: Vector[AggregateWarning]) = AggregateWarning.save(models.toList)
}

object AggregateWarningWriter {
  def name(id: String): String = s"$id-agg-warning-writer"
  def props(id: String, eventLog: ActorRef): Props =
    Props(new AggregateWarningWriter(id, eventLog)).withDispatcher("dispatchers.writer")
}
