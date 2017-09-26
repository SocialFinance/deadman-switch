package org.sofi.deadman.location

import akka.actor._
import com.rbmhtechnology.eventuate._
import com.rbmhtechnology.eventuate.log.cassandra._
import org.sofi.deadman.component.actor._
import org.sofi.deadman.component.processor._
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
  private val endpoint = new ReplicationEndpoint(id, Set("V1", "R1"), logId ⇒ CassandraEventLog.props(logId), connections)
  endpoint.activate()

  // Event Logs
  private val eventLog = endpoint.logs("V1")
  private val reportLog = endpoint.logs("R1")

  // Processors and Writers
  private val tagWriter = system.actorOf(TaggedExpirationWriter.props(s"$id-tagged-expiration-writer", reportLog))
  private val _ = (
    system.actorOf(TaskExpirationProcessor.props(s"$id-task-expiration-processor", eventLog, reportLog)),
    system.actorOf(TaskExpirationWriter.props(s"$id-task-expiration-writer", eventLog)),
    system.actorOf(TaskWarningWriter.props(s"$id-task-warning-writer", eventLog))
  )

  // Command
  val commandManager = system.actorOf(TaskManager.props(s"$id-task-manager", eventLog))

  // Query
  private val aggregateView = system.actorOf(AggregateView.props(s"$id-aggregate-view", eventLog))
  private val entityView = system.actorOf(EntityView.props(s"$id-entity-view", eventLog))
  val queryManager = system.actorOf(QueryManager.props(aggregateView, entityView, tagWriter))
}
