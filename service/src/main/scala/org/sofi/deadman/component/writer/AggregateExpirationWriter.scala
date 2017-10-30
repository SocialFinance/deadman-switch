package org.sofi.deadman.component.writer

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._

final class AggregateExpirationWriter(val id: String, val eventLog: ActorRef) extends TaskWriter[AggregateExpiration] {

  // Writer ID
  val writerId = "AggregateExpirationWriter"

  // Query for expired tasks
  def onCommand: Receive = {
    case q: GetExpirations ⇒
      val _ = AggregateExpiration.select(q.aggregate.getOrElse("")).map { result ⇒
        Tasks(result.map(_.asTask))
      } recoverWith noTasks pipeTo sender()
  }

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaskExpiration(t, exp) ⇒
      batch(AggregateExpiration(t.aggregate, t.entity, t.key, t.ttl, t.ts, exp, t.tags.sorted.mkString(",")))
  }

  // Save a series of aggregate expirations to C*
  def write(models: Vector[AggregateExpiration]) = AggregateExpiration.save(models.toList)
}

object AggregateExpirationWriter {
  def name(id: String): String = s"$id-agg-expiration-writer"
  def props(id: String, eventLog: ActorRef): Props =
    Props(new AggregateExpirationWriter(id, eventLog)).withDispatcher("dispatchers.writer")
}
