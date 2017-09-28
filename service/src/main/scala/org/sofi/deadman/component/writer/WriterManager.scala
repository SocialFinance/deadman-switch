package org.sofi.deadman.component.writer

import akka.actor._
import org.sofi.deadman.messages.query._

final class WriterManager(val id: String, val eventLog: ActorRef, val targetLog: ActorRef) extends Actor {

  // Writes and handles queries for aggregate expiration events
  private val aggExpWriter = context.actorOf(AggregateExpirationWriter.props(AggregateExpirationWriter.name(id), eventLog))

  // Writes and handles queries for aggregate task warnings
  private val aggWarnWriter = context.actorOf(AggregateWarningWriter.props(AggregateWarningWriter.name(id), eventLog))

  // Writes and handles queries for entity expiration events
  private val entExpWriter = context.actorOf(EntityExpirationWriter.props(EntityExpirationWriter.name(id), eventLog))

  // Writes and handles queries for entity task warnings
  private val entWarnWriter = context.actorOf(EntityWarningWriter.props(EntityWarningWriter.name(id), eventLog))

  // Writes and handles queries for tagged task expiration events
  private val tagWriter = context.actorOf(TaggedExpirationWriter.props(TaggedExpirationWriter.name(id), targetLog))

  // Forward queries to the appropriate writer
  def receive: Receive = {
    case query: GetTags ⇒ tagWriter forward query
    case query: GetExpirations ⇒ query.queryType match {
      case QueryType.ENTITY ⇒ entExpWriter forward query
      case _ ⇒ aggExpWriter forward query
    }
    case query: GetWarnings ⇒ query.queryType match {
      case QueryType.ENTITY ⇒ entWarnWriter forward query
      case _ ⇒ aggWarnWriter forward query
    }
  }
}

object WriterManager {
  def props(id: String, eventLog: ActorRef, targetLog: ActorRef): Props = Props(new WriterManager(id, eventLog, targetLog))
}
