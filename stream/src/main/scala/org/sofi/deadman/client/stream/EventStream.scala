package org.sofi.deadman.client.stream

import akka.actor._
import akka.stream.scaladsl._
import com.rbmhtechnology.eventuate._
import com.rbmhtechnology.eventuate.adapter.stream._
import com.rbmhtechnology.eventuate.log.leveldb._
import com.trueaccord.scalapb.GeneratedMessage
import com.trueaccord.scalapb.json.JsonFormat
import com.typesafe.config.Config
import org.json4s.JsonAST._
import org.json4s.jackson.JsonMethods

// An Akka stream source of all events. NOTE: event delivery is 'at least once', meaning duplicate warning and expirations events will
// be received when multiple service locations are active.
final class EventStream(val settings: StreamSettings)(implicit val system: ActorSystem) {

  // Connect to a replication endpoint and activate
  private val connection = ReplicationConnection(settings.host, settings.port, system.name)
  private val endpoint =
    new ReplicationEndpoint(settings.id, Set(settings.logName), logId ⇒ LeveldbEventLog.props(logId), Set(connection))
  endpoint.activate()

  // Event log
  private val eventLog = endpoint.logs(settings.logName)

  // Event source
  private val eventSource =
    DurableEventSource(eventLog, fromSequenceNr = settings.offset.getOrElse(0L), aggregateId = settings.aggregate)

  // Akka stream based event source
  val events = Source.fromGraph(eventSource)

  // Akka stream based JSON event source
  val jsonEvents = Source.fromGraph(eventSource).map { e ⇒
    val obj = JsonFormat.toJson(e.payload.asInstanceOf[GeneratedMessage])
    val objType = JObject(("type", JString(e.payload.getClass.getSimpleName)))
    JsonMethods.compact(JsonMethods.render(obj merge objType))
  }
}

object EventStream {
  def actorSystem(config: Config) = ActorSystem("deadman-switch-actor-system", config)
  def apply(settings: StreamSettings)(implicit system: ActorSystem) = new EventStream(settings)
}
