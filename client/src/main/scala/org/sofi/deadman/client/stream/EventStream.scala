package org.sofi.deadman.client.stream

import akka.actor._
import akka.stream.scaladsl._
import com.rbmhtechnology.eventuate._
import com.rbmhtechnology.eventuate.adapter.stream._
import com.rbmhtechnology.eventuate.log.leveldb._
import com.typesafe.config.Config

final class EventStream(val settings: StreamSettings)(implicit val system: ActorSystem) {

  // Connect to a replication endpoint and activate
  private val connection = ReplicationConnection(settings.host, settings.port, system.name)
  private val endpoint =
    new ReplicationEndpoint(settings.id, Set("L1"), logId ⇒ LeveldbEventLog.props(logId), Set(connection))
  endpoint.activate()

  // Event log
  private val eventLog = endpoint.logs("L1")

  // Event source
  private val eventSource =
    DurableEventSource(eventLog, fromSequenceNr = settings.offset.getOrElse(0L), aggregateId = settings.aggregate)

  // Akka stream based event source
  val events = Source.fromGraph(eventSource).filter(e ⇒ settings.filter(e.payload))
}

object EventStream {
  def actorSystem(config: Config) = ActorSystem("deadman-switch-actor-system", config)
  def apply(settings: StreamSettings)(implicit system: ActorSystem) = new EventStream(settings)
}
