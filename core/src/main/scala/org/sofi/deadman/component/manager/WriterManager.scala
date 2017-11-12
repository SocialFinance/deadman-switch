package org.sofi.deadman.component.manager

import akka.actor._
import org.sofi.deadman.component.writer.expiration._
import org.sofi.deadman.component.writer.warning._
import org.sofi.deadman.log._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.messages.query.QueryType._

final class WriterManager(val id: String, eventLogs: Map[String, ActorRef]) extends Actor {

  // Core event log
  private val eventLog = eventLogs(EventLog.name)

  // Writes and handles queries for aggregate expiration events
  private val aggExpWriter = context.actorOf(AggregateExpirationWriter.props(AggregateExpirationWriter.name(id), eventLog))

  // Writes and handles queries for aggregate task warnings
  private val aggWarnWriter = context.actorOf(AggregateWarningWriter.props(AggregateWarningWriter.name(id), eventLog))

  // Writes and handles queries for entity expiration events
  private val entExpWriter = context.actorOf(EntityExpirationWriter.props(EntityExpirationWriter.name(id), eventLog))

  // Writes and handles queries for entity task warnings
  private val entWarnWriter = context.actorOf(EntityWarningWriter.props(EntityWarningWriter.name(id), eventLog))

  // Writes and handles queries for tagged task expiration events
  private val tagWriter = context.actorOf(TaggedExpirationWriter.props(TaggedExpirationWriter.name(id), eventLogs(TagLog.name)))

  // Writes and handles queries for keyed task expiration events
  private val keyWriter = context.actorOf(KeyExpirationWriter.props(KeyExpirationWriter.name(id), eventLogs(KeyLog.name)))

  // Forward queries to the appropriate writer
  def receive: Receive = {
    case query: GetByTag ⇒ tagWriter forward query
    case query: GetByKey ⇒ keyWriter forward query
    case query: GetExpirations ⇒ query.queryType match {
      case ENTITY ⇒ entExpWriter forward query
      case _ ⇒ aggExpWriter forward query
    }
    case query: GetWarnings ⇒ query.queryType match {
      case ENTITY ⇒ entWarnWriter forward query
      case _ ⇒ aggWarnWriter forward query
    }
  }
}

object WriterManager {
  def props(id: String, eventLogs: Map[String, ActorRef]): Props = Props(new WriterManager(id, eventLogs))
}
