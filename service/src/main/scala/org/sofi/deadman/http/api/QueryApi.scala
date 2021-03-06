package org.sofi.deadman.http.api

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.sofi.deadman.messages.query._
import scala.concurrent.Future

final class QueryApi(queryManager: ActorRef)(implicit val system: ActorSystem, timeout: Timeout) {

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
    (queryManager ? GetByTag(tag, window)).mapTo[Tasks]

  // Get all expired tasks by key within a given time window
  def queryExpiredKey(key: String, window: String) =
    (queryManager ? GetByKey(key, window)).mapTo[Tasks]

  // Warnings -------------------------------------------------------------------------------------------------------------------------

  // Get all task warnings for the given aggregate ID
  def queryAggregateWarnings(id: String): Future[Tasks] =
    (queryManager ? GetWarnings(QueryType.AGGREGATE, aggregate = Some(id))).mapTo[Tasks]

  // Get all task warnings for the given entity ID
  def queryEntityWarnings(id: String): Future[Tasks] =
    (queryManager ? GetWarnings(QueryType.ENTITY, entity = Some(id))).mapTo[Tasks]
}
