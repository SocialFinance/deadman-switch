package org.sofi.deadman.model

final case class WriteProgress(id: String, sequenceNr: Long)

object WriteProgress {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.storage._, db._

  // Read the current sequence number from C*
  def read(id: String)(implicit ec: ExecutionContext): Future[Long] =
    db.run {
      quote {
        query[WriteProgress].filter(_.id == lift(id)).map(_.sequenceNr)
      }
    } map {
      _.headOption.getOrElse(0L)
    }

  // Update the current sequence number in C*
  def write(id: String, sequenceNr: Long)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[WriteProgress].filter(_.id == lift(id)).update(_.sequenceNr -> lift(sequenceNr))
      }
    }
}
