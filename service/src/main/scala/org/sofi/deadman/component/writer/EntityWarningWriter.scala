package org.sofi.deadman.component.writer

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._
import scala.concurrent.Future

final class EntityWarningWriter(val id: String, val eventLog: ActorRef) extends TaskWriter with ActorLogging {

  // Writer ID
  val writerId = "EntityWarningWriter"

  // Implicit execution context
  import context.dispatcher

  // Batch models during event processing.
  private var batch: Vector[EntityWarning] = Vector.empty

  // Entity query for task warnings
  override def onCommand = {
    case q: GetWarnings ⇒
      val _ = EntityWarning.select(q.entity.getOrElse("")).map { result ⇒
        Tasks(result.map(w ⇒ Task(w.key, w.aggregate, w.entity, w.creation, w.ttl, Seq(w.ttw), w.tags.split(","))))
      } recoverWith {
        case t: Throwable ⇒
          log.warning("Warning query exception", t)
          Future.successful(Tasks(Seq.empty))
      } pipeTo sender()
  }

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaskWarning(t, ttw) ⇒
      val tags = t.tags.sorted.mkString(",")
      val expiration = EntityWarning(t.entity, t.key, ttw, t.aggregate, t.ttl, t.ts, System.currentTimeMillis(), tags)
      batch = batch :+ expiration
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

object EntityWarningWriter {
  def name(id: String): String = s"$id-ent-warning-writer"
  def props(id: String, eventLog: ActorRef): Props = Props(new EntityWarningWriter(id, eventLog))
}
