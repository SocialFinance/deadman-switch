package org.sofi.deadman.component.writer

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._

final class AggregateWarningWriter(val id: String, val eventLog: ActorRef) extends TaskWriter[AggregateWarning] {

  // Writer ID
  val writerId = "AggregateWarningWriter"

  // Aggregate query for task warnings
  override def onCommand: Receive = {
    case q: GetWarnings ⇒
      val _ = AggregateWarning.select(q.aggregate.getOrElse("")).map { result ⇒
        Tasks(result.map(w ⇒ Task(w.key, w.aggregate, w.entity, w.creation, w.creation, Seq(w.ttw), w.tags.split(","))))
      } recoverWith noTasks pipeTo sender()
  }

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaskWarning(t, ttw) ⇒
      cache(AggregateWarning(t.aggregate, t.key, ttw, t.entity, t.ttl, t.ts, System.currentTimeMillis(), t.tags.sorted.mkString(",")))
  }

  // Save an aggregate warning to C*
  override def save(model: AggregateWarning) = model.save
}

object AggregateWarningWriter {
  def name(id: String): String = s"$id-agg-warning-writer"
  def props(id: String, eventLog: ActorRef): Props = Props(new AggregateWarningWriter(id, eventLog))
}
