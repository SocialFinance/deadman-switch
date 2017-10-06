package org.sofi.deadman.http

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import scala.concurrent.{ ExecutionContext, Future }

final class ApiFunctions(commandManager: ActorRef, queryManager: ActorRef)(implicit val ec: ExecutionContext, val timeout: Timeout) {

  // Helper function for parsing `ttw` values
  private def parseTTW(ttw: Option[String]): Seq[Long] = {
    val maybeWarnings: Option[Seq[Long]] = ttw.map(_.split(",").map(_.toLong))
    maybeWarnings.getOrElse(Seq.empty)
  }

  // Helper function for parsing `tag` values
  private def parseTags(tags: Option[String]): Seq[String] = {
    val seq: Option[Seq[String]] = tags.map(_.split(",").toSeq)
    seq.getOrElse(Seq.empty)
  }

  // Schedule a task
  def scheduleTask(key: String, agg: String, ent: String, ttl: Long, ttw: Option[String], tags: Option[String], ts: Option[Long]) =
    commandManager.ask(
      ScheduleTask(key, agg, ent, ttl, parseTTW(ttw), parseTags(tags), ts)
    ).mapTo[CommandResponse]

  // Complete a task
  def completeTask(key: String, agg: String, ent: String): Future[CommandResponse] =
    commandManager.ask(
      CompleteTask(key, agg, ent)
    ).mapTo[CommandResponse]

  // Get all active tasks for the given aggregate ID
  def queryAggregate(id: String): Future[Tasks] =
    queryManager.ask(
      GetTasks(QueryType.AGGREGATE, aggregate = Some(id))
    ).mapTo[Tasks]

  // Get all active tasks for the given entity ID
  def queryEntity(id: String): Future[Tasks] =
    queryManager.ask(
      GetTasks(QueryType.ENTITY, entity = Some(id))
    ).mapTo[Tasks]

  // Get all expired tasks for the given aggregate ID
  def queryAggregateExpirations(id: String): Future[Tasks] =
    queryManager.ask(
      GetExpirations(QueryType.AGGREGATE, aggregate = Some(id))
    ).mapTo[Tasks]

  // Get all task warnings for the given aggregate ID
  def queryAggregateWarnings(id: String): Future[Tasks] =
    queryManager.ask(
      GetWarnings(QueryType.AGGREGATE, aggregate = Some(id))
    ).mapTo[Tasks]

  // Count all scheduled tasks for the given aggregate ID
  def queryAggregateCount(id: String): Future[Count] =
    queryManager.ask(
      GetCount(QueryType.AGGREGATE, aggregate = Some(id))
    ).mapTo[Count]

  // Get all expired tasks for the given entity ID
  def queryEntityExpirations(id: String): Future[Tasks] =
    queryManager.ask(
      GetExpirations(QueryType.ENTITY, entity = Some(id))
    ).mapTo[Tasks]

  // Get all task warnings for the given entity ID
  def queryEntityWarnings(id: String): Future[Tasks] =
    queryManager.ask(
      GetWarnings(QueryType.ENTITY, entity = Some(id))
    ).mapTo[Tasks]

  // Count all task warnings for the given entity ID
  def queryEntityCount(id: String): Future[Count] =
    queryManager.ask(
      GetCount(QueryType.ENTITY, entity = Some(id))
    ).mapTo[Count]

  // Get all expired tasks with the given tag for a time window
  def queryExpiredTag(tag: String, window: String) =
    queryManager.ask(
      GetTags(tag, window)
    ).mapTo[Tasks]
}
