package org.sofi.deadman.component.writer

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._
import scala.concurrent.Future

final class TaggedExpirationWriter(val id: String, val eventLog: ActorRef) extends TaskWriter with ActorLogging {

  // Writer ID
  val writerId = "TaggedExpirationWriter"

  // Implicit execution context
  import context.dispatcher

  // Batch models during event processing.
  private var batch: Vector[TaggedViolation] = Vector.empty

  // Query C* for tagged violations
  override def onCommand = {
    case q: GetTags ⇒
      val start = q.start.getOrElse(Long.MinValue)
      val end = q.end.getOrElse(Long.MaxValue)
      val _ = TaggedViolation.select(q.tag, q.window, start, end) map { result ⇒
        Tasks(result.map(v ⇒ Task(v.key, v.aggregate, v.entity, v.creation, v.ttl, Seq.empty, v.tags.split(","))))
      } recoverWith {
        case _: Throwable ⇒ Future.successful(Tasks(Seq.empty))
      } pipeTo sender()
  }

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaggedExpiration(task, tag, window, ts) ⇒
      val tags = task.tags.sorted.mkString(",")
      val violation = TaggedViolation(tag, window, ts, task.aggregate, task.entity, task.key, task.ttl, task.ts, tags)
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

object TaggedExpirationWriter {
  def props(id: String, eventLog: ActorRef): Props = Props(new TaggedExpirationWriter(id, eventLog))
}
