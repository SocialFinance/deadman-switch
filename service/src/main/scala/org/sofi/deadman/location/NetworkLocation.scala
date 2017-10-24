package org.sofi.deadman.location

import akka.actor._
import com.rbmhtechnology.eventuate._
import com.rbmhtechnology.eventuate.log.cassandra._
import org.sofi.deadman.component.actor._
import org.sofi.deadman.component.processor._
import org.sofi.deadman.component.query._
import org.sofi.deadman.component.view._
import org.sofi.deadman.component.writer._
import scala.collection.JavaConverters._

final class NetworkLocation(val id: String)(implicit system: ActorSystem) {

  // Config
  private val config = system.settings.config
  private val host = config.getString("akka.remote.netty.tcp.hostname")
  private val port = config.getInt("akka.remote.netty.tcp.port")

  // Replication endpoint
  private val cluster = config.getStringList("replication.endpoint.cluster")
  private val connections = cluster.asScala.filterNot(_ == s"$host:$port").toSet.map { address: String ⇒
    val Array(host, port) = address.split(":")
    ReplicationConnection(host, port.toInt, system.name)
  }
  private val endpoint = new ReplicationEndpoint(id, Set("L1", "T1", "K1"), logId ⇒ CassandraEventLog.props(logId), connections)
  endpoint.activate()

  // Event Logs
  private val eventLog = endpoint.logs("L1")
  private val tagLog = endpoint.logs("T1")
  private val keyLog = endpoint.logs("K1")

  // Event sourced components
  system.actorOf(TaggedExpirationProcessor.props(TaggedExpirationProcessor.name(id), eventLog, tagLog))
  system.actorOf(KeyExpirationProcessor.props(KeyExpirationProcessor.name(id), eventLog, keyLog))
  private val writerManager = system.actorOf(WriterManager.props(id, eventLog, tagLog, keyLog))
  private val viewManager = system.actorOf(ViewManager.props(id, eventLog))
  val queryManager = system.actorOf(QueryManager.props(viewManager, writerManager))
  val commandManager = system.actorOf(TaskManager.props(TaskManager.name(id), eventLog))
}
