package org.sofi.deadman.component.writer

import akka.actor._
import akka.pattern.pipe
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._
import scala.concurrent.Future

final class TaskWarningWriter(val id: String, val eventLog: ActorRef) extends TaskWriter with ActorLogging {

  // Writer ID
  val writerId = "TaskWarningWriter"

  // Implicit execution context
  import context.dispatcher

  // Batch models during event processing.
  private var batch: Vector[Warning] = Vector.empty

  // Convert events to models and batch. Note: An event handler should never write to the database directly.
  def onEvent = {
    case TaskWarning(t, ttw) ⇒
      val tags = t.tags.sorted.mkString(",")
      val warning = Warning(t.aggregate, t.entity, t.key, ttw, t.ts, System.currentTimeMillis(), tags)
      batch = batch :+ warning
  }

  // Query for task warnings
  override def onCommand: Receive = {
    case q: GetWarnings ⇒
      val _ = Warning.select(q.aggregate).map { result ⇒
        Tasks(result.map(w ⇒ Task(w.key, w.aggregate, w.entity, w.creation, w.creation, Seq(w.ttw), w.tags.split(","))))
      } recoverWith {
        case t: Throwable ⇒
          log.warning("Warning query exception", t)
          Future.successful(Tasks(Seq.empty))
      } pipeTo sender()
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

object TaskWarningWriter {
  def props(id: String, eventLog: ActorRef): Props = Props(new TaskWarningWriter(id, eventLog))
}
