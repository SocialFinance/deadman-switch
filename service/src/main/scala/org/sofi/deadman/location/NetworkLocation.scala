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
  private val endpoint = new ReplicationEndpoint(id, Set("L1", "W1"), logId ⇒ CassandraEventLog.props(logId), connections)
  endpoint.activate()

  // Event Logs
  private val eventLog = endpoint.logs("L1")
  private val reportLog = endpoint.logs("W1")

  // Writers
  private val expirationWriter = system.actorOf(TaskExpirationWriter.props(s"$id-task-expiration-writer", eventLog))
  private val warningWriter = system.actorOf(TaskWarningWriter.props(s"$id-task-warning-writer", eventLog))
  private val tagWriter = system.actorOf(TaggedExpirationWriter.props(s"$id-tagged-expiration-writer", reportLog))
  private val writerManager = system.actorOf(WriterManager.props(expirationWriter, warningWriter, tagWriter))

  // Processor
  private val _ = system.actorOf(TaskExpirationProcessor.props(s"$id-task-expiration-processor", eventLog, reportLog))

  // Command
  val commandManager = system.actorOf(TaskManager.props(s"$id-task-manager", eventLog))

  // Views
  private val aggregateView = system.actorOf(AggregateView.props(s"$id-aggregate-view", eventLog))
  private val entityView = system.actorOf(EntityView.props(s"$id-entity-view", eventLog))
  private val keyView = system.actorOf(KeyView.props(s"$id-key-view", eventLog))
  private val viewManager = system.actorOf(ViewManager.props(aggregateView, entityView, keyView))

  // Queries
  val queryManager = system.actorOf(QueryManager.props(viewManager, writerManager))
}
