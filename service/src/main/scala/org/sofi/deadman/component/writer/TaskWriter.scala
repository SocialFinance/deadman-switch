package org.sofi.deadman.component.writer

import com.rbmhtechnology.eventuate.EventsourcedWriter

trait TaskWriter extends EventsourcedWriter[Long, Unit] {

  // The ID of this writer
  def writerId: String

  // Event replay back-pressure: replay is suspended after a set number of events and a write is triggered.
  // This is necessary when writing to the database is slower than replaying from the eventLog (which is usually the case).
  override def replayBatchSize: Int = 1024

  // Ignore commands by default
  def onCommand: Receive = {
    case _ ⇒
  }

  // Indicates the start position for further reads from the event log.
  override def readSuccess(result: Long): Option[Long] = Some(result + 1L)
}
