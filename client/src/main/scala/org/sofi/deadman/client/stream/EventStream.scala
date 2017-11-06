package org.sofi.deadman.client.stream

import akka.stream.scaladsl._
import com.rbmhtechnology.eventuate._
import com.rbmhtechnology.eventuate.adapter.stream._
import com.rbmhtechnology.eventuate.log.leveldb._
import org.sofi.deadman.client._

final class EventStream(val settings: Settings) {

  // Connect to a replication endpoint and activate
  private val connection = ReplicationConnection(settings.host, settings.port, actorSystem.name)
  private val endpoint =
    new ReplicationEndpoint(settings.id, Set(eventLogName), logId ⇒ LeveldbEventLog.props(logId), Set(connection))
  endpoint.activate()

  // Event log
  private val eventLog = endpoint.logs(eventLogName)

  // Event source
  private val eventSource =
    DurableEventSource(eventLog, fromSequenceNr = settings.offset.getOrElse(0L), aggregateId = settings.aggregate)

  // Akka stream based event source
  val events = Source.fromGraph(eventSource).filter(e ⇒ settings.filter(e.payload))
}

object EventStream {
  def apply(settings: Settings) = new EventStream(settings)
}
