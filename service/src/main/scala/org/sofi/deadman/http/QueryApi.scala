package org.sofi.deadman.http

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.sofi.deadman.messages.query._
import scala.concurrent.Future

final class QueryApi(queryManager: ActorRef)(implicit val sys: ActorSystem, val t: Timeout) {

  // Active ---------------------------------------------------------------------------------------------------------------------------

  // Get all active tasks for the given aggregate ID
  def queryAggregate(id: String): Future[Tasks] =
    (queryManager ? GetTasks(QueryType.AGGREGATE, aggregate = Some(id))).mapTo[Tasks]

  // Get all scheduled tasks for the given entity ID
  def queryEntity(id: String): Future[Tasks] =
    (queryManager ? GetTasks(QueryType.ENTITY, entity = Some(id))).mapTo[Tasks]

  // Get all scheduled tasks for the given key
  def queryKey(value: String): Future[Tasks] =
    (queryManager ? GetTasks(QueryType.KEY, key = Some(value))).mapTo[Tasks]

  // Expirations ----------------------------------------------------------------------------------------------------------------------

  // Get all expired tasks for the given aggregate ID
  def queryAggregateExpirations(id: String): Future[Tasks] =
    (queryManager ? GetExpirations(QueryType.AGGREGATE, aggregate = Some(id))).mapTo[Tasks]

  // Get all expired tasks for the given entity ID
  def queryEntityExpirations(id: String): Future[Tasks] =
    (queryManager ? GetExpirations(QueryType.ENTITY, entity = Some(id))).mapTo[Tasks]

  // Get all expired tasks by tag within a given time window
  def queryExpiredTag(tag: String, window: String) =
    (queryManager ? GetTags(tag, window)).mapTo[Tasks]

  // Warnings -------------------------------------------------------------------------------------------------------------------------

  // Get all task warnings for the given aggregate ID
  def queryAggregateWarnings(id: String): Future[Tasks] =
    (queryManager ? GetWarnings(QueryType.AGGREGATE, aggregate = Some(id))).mapTo[Tasks]

  // Get all task warnings for the given entity ID
  def queryEntityWarnings(id: String): Future[Tasks] =
    (queryManager ? GetWarnings(QueryType.ENTITY, entity = Some(id))).mapTo[Tasks]

  // Counts ---------------------------------------------------------------------------------------------------------------------------

  // Count all scheduled tasks for the given aggregate ID
  def queryAggregateCount(id: String): Future[Count] =
    (queryManager ? GetCount(QueryType.AGGREGATE, aggregate = Some(id))).mapTo[Count]

  // Count all task warnings for the given entity ID
  def queryEntityCount(id: String): Future[Count] =
    (queryManager ? GetCount(QueryType.ENTITY, entity = Some(id))).mapTo[Count]

  // Count all scheduled tasks for the given key
  def queryKeyCount(value: String): Future[Count] =
    (queryManager ? GetCount(QueryType.KEY, key = Some(value))).mapTo[Count]
}
