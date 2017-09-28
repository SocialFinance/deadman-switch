package org.sofi.deadman.http

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import scala.concurrent.{ ExecutionContext, Future }

final class ApiFunctions(commandManager: ActorRef, queryManager: ActorRef)(implicit val ec: ExecutionContext, val timeout: Timeout) {

  // Schedule a task
  def scheduleTask(key: String, agg: String, ent: String, ttw: String, ttl: Long, tags: String): Future[CommandResponse] =
    commandManager.ask(
      ScheduleTask(key, agg, ent, ttl, ttw.split(',').map(_.toLong), tags.split(','))
    ).mapTo[CommandResponse]

  // Complete a task
  def completeTask(key: String, agg: String, ent: String): Future[CommandResponse] =
    commandManager.ask(
      CompleteTask(key, agg, ent)
    ).mapTo[CommandResponse]

  // Get all active tasks for the given aggregate ID
  def queryAggregate(id: String): Future[Tasks] =
    queryManager.ask(
      GetTasks(view = GetTasks.ViewType.AGGREGATE, aggregate = Some(id))
    ).mapTo[Tasks]

  // Get all active tasks for the given entity ID
  def queryEntity(id: String): Future[Tasks] =
    queryManager.ask(
      GetTasks(view = GetTasks.ViewType.ENTITY, entity = Some(id))
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

  // Get all expired tasks with the given tag for a time window
  def queryExpiredTag(tag: String, window: String) =
    queryManager.ask(
      GetTags(tag, window)
    ).mapTo[Tasks]
}
