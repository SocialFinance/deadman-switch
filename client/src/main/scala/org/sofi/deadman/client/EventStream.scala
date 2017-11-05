package org.sofi.deadman.client

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import com.rbmhtechnology.eventuate._
import com.rbmhtechnology.eventuate.adapter.stream._
import com.rbmhtechnology.eventuate.log.leveldb._
import com.typesafe.config.ConfigFactory

final class EventStream(val settings: Settings)(implicit system: ActorSystem, materializer: ActorMaterializer) {

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
  private val name = "deadman-switch-actor-system"
  implicit val system: ActorSystem = ActorSystem(name, ConfigFactory.load().resolve())
  implicit val materializer = ActorMaterializer()
  def apply(settings: Settings) = new EventStream(settings)
}
