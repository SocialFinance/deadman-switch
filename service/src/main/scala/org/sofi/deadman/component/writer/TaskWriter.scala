package org.sofi.deadman.component.writer

import akka.actor.ActorLogging
import com.rbmhtechnology.eventuate.EventsourcedWriter
import org.sofi.deadman.messages.query._
import org.sofi.deadman.model._
import scala.concurrent.Future
import scala.util.control.NonFatal

trait TaskWriter[T] extends EventsourcedWriter[Long, Unit] with ActorLogging {

  // The ID of this writer
  def writerId: String

  // Implicit execution context
  protected implicit val ec = context.dispatcher

  // Batch models during event processing.
  private var batch: Vector[T] = Vector.empty

  // Event replay back-pressure: replay is suspended after a set number of events and a write is triggered.
  // This is necessary when writing to the database is slower than replaying from the eventLog (which is usually the case).
  override def replayBatchSize: Int = 1024

  // Reads the sequence number of the last update; called only once after writer start or restart.
  def read(): Future[Long] = WriteProgress.read(writerId)

  // Indicates the start position for further reads from the event log.
  override def readSuccess(result: Long): Option[Long] = Some(result + 1L)

  // Asynchronously writes the batch and sequence number of the last processed event to the database.
  def write(): Future[Unit] = {
    val nr = lastSequenceNr
    val res = for {
      _ ← Future.sequence(batch.map(save))
      _ ← WriteProgress.write(writerId, nr)
    } yield ()
    batch = Vector.empty // clear so that events can be processed while the write is in progress.
    res
  }

  // Add a model to the batch collection
  def cache(t: T): Unit = batch = batch :+ t

  // Generic future error handler function
  def noTasks: PartialFunction[Throwable, Future[Tasks]] = {
    case NonFatal(_) ⇒
      Future.successful(Tasks(Seq.empty))
  }

  // Save a model to a DB
  def save(t: T): Future[Unit]
}
