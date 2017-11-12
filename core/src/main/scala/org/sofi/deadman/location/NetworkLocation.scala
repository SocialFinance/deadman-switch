package org.sofi.deadman.location

import akka.actor._
import com.rbmhtechnology.eventuate._
import com.rbmhtechnology.eventuate.log.cassandra._
import org.sofi.deadman.component.manager._
import org.sofi.deadman.component.processor._
import org.sofi.deadman.log._

final class NetworkLocation(val id: String)(implicit system: ActorSystem) {

  // Config
  private val config = system.settings.config

  // Replication endpoint
  private val cluster = config.getString("replication.endpoints").split(",")
  private val connections = cluster.toSet.map { address: String ⇒
    val Array(host, port) = address.split(":")
    ReplicationConnection(host, port.toInt, system.name)
  }
  private val endpoint = new ReplicationEndpoint(id, logNames, logId ⇒ CassandraEventLog.props(logId), connections)
  endpoint.activate()

  // Event Logs
  private val actorLogs = endpoint.logs
  val eventLog = actorLogs(EventLog.name)

  // Event sourced components
  system.actorOf(TaggedExpirationProcessor.props(TaggedExpirationProcessor.name(id), actorLogs))
  system.actorOf(KeyExpirationProcessor.props(KeyExpirationProcessor.name(id), actorLogs))
  private val writerManager = system.actorOf(WriterManager.props(id, actorLogs))
  private val viewManager = system.actorOf(ViewManager.props(id, eventLog))
  val queryManager = system.actorOf(QueryManager.props(viewManager, writerManager))
  val commandManager = system.actorOf(CommandManager.props(CommandManager.name(id), eventLog))
}
