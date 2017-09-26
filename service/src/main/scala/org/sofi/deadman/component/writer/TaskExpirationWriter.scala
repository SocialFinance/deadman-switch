package org.sofi.deadman.component.writer

import akka.actor._
import org.sofi.deadman.messages.event._
import org.sofi.deadman.model._
import scala.concurrent.Future

final class TaskExpirationWriter(val id: String, val eventLog: ActorRef) extends TaskWriter {

  // Writer ID
  val writerId = "TaskExpirationWriter"

  // Implicit execution context
  import context.dispatcher

  // Batch models during event processing.
  private var batch: Vector[Violation] = Vector.empty

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaskExpiration(t, exp) ⇒
      val tags = t.tags.sorted.mkString(",")
      val violation = Violation(t.aggregate, t.entity, t.key, t.ttl, t.ts, exp, tags)
      batch = batch :+ violation
  }

  // Reads the sequence number of the last update; called only once after writer start or restart.
  def read(): Future[Long] = WriteProgress.read(writerId)

  // Asynchronously writes the batch and sequence number of the last processed event to the database.
  def write(): Future[Unit] = {
    val nr = lastSequenceNr
    val res = for {
      _ ← Future.sequence(batch.map(_.save))
      _ ← WriteProgress.write(writerId, nr)
    } yield ()
    batch = Vector.empty // clear so that events can be processed while the write is in progress.
    res
  }
}

object TaskExpirationWriter {
  def props(id: String, eventLog: ActorRef): Props = Props(new TaskExpirationWriter(id, eventLog))
}
