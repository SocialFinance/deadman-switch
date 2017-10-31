package org.sofi.deadman.component.writer

import akka.actor.ActorLogging
import com.rbmhtechnology.eventuate.EventsourcedWriter
import org.sofi.deadman.model._
import scala.concurrent.Future

trait TaskWriter[T] extends EventsourcedWriter[Long, Unit] with ActorLogging with NoTasks {

  // Implicit execution context
  protected implicit val executionContext = context.dispatcher

  // Batch models during event processing.
  private var cache: Vector[T] = Vector.empty

  // Add a model to the cache collection
  def batch(t: T): Unit = cache = cache :+ t

  // Asynchronously writes the cache and sequence number of the last processed event to the database.
  def write(): Future[Unit] = {
    val nr = lastSequenceNr
    val res = for {
      _ ← write(cache)
      _ ← WriteProgress.write(writerId, nr)
    } yield ()
    cache = Vector.empty // clear so that events can be processed while the write is in progress.
    res
  }

  // Reads the sequence number of the last update; called only once after writer start or restart.
  def read(): Future[Long] = WriteProgress.read(writerId)

  // Indicates the start position for further reads from the event log.
  override def readSuccess(result: Long): Option[Long] = Some(result + 1L)

  // The ID of this writer
  def writerId: String

  // Save a series of models to a DB
  def write(t: Vector[T]): Future[Unit]
}
