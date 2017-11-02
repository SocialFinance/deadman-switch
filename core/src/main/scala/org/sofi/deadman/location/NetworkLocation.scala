package org.sofi.deadman.location

import akka.actor._
import com.rbmhtechnology.eventuate._
import com.rbmhtechnology.eventuate.log.cassandra._
import org.sofi.deadman.component.manager._
import org.sofi.deadman.component.processor._

final class NetworkLocation(val id: String)(implicit system: ActorSystem) {

  // Config
  private val config = system.settings.config

  // Replication endpoint
  private val cluster = config.getString("replication.endpoints").split(",")
  private val connections = cluster.toSet.map { address: String ⇒
    val Array(host, port) = address.split(":")
    ReplicationConnection(host, port.toInt, system.name)
  }
  private val endpoint = new ReplicationEndpoint(id, Set("L1", "T1", "K1"), logId ⇒ CassandraEventLog.props(logId), connections)
  endpoint.activate()

  // Event Logs
  val eventLog = endpoint.logs("L1")
  private val tagLog = endpoint.logs("T1")
  private val keyLog = endpoint.logs("K1")

  // Event sourced components
  system.actorOf(TaggedExpirationProcessor.props(TaggedExpirationProcessor.name(id), eventLog, tagLog))
  system.actorOf(KeyExpirationProcessor.props(KeyExpirationProcessor.name(id), eventLog, keyLog))
  private val writerManager = system.actorOf(WriterManager.props(id, eventLog, tagLog, keyLog))
  private val viewManager = system.actorOf(ViewManager.props(id, eventLog))
  val queryManager = system.actorOf(QueryManager.props(viewManager, writerManager))
  val commandManager = system.actorOf(CommandManager.props(CommandManager.name(id), eventLog))
}
