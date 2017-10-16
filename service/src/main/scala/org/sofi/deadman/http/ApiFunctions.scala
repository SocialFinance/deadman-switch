package org.sofi.deadman.http

import akka.actor._
import akka.pattern.ask
import akka.stream._
import akka.stream.scaladsl._
import akka.util.Timeout
import org.sofi.deadman.messages.command._, CommandResponse.ResponseType._
import org.sofi.deadman.messages.query._
import scala.concurrent.Future
import scala.concurrent.duration._

final class ApiFunctions(commandManager: ActorRef, queryManager: ActorRef)(implicit val sys: ActorSystem, val t: Timeout, val am: ActorMaterializer) {

  // Execution context
  import sys.dispatcher

  // Queue buffer constant
  private val BUFFER_SIZE = 10000

  // Stream is chunked into groups of elements received within a time window
  private val GROUP_SIZE = 1000

  // Stream group time window
  private val TIME_WINDOW = 1.second

  // Stream parallelism setting
  private val PARALLELISM = Runtime.getRuntime.availableProcessors()

  // The following Akka Streams implementation batches writes to the command manager, buffering messages until the buffer size
  // is reached -or- a given amount of time passes. It also limits the number of outstanding asynchronous `ask` calls.
  private val queue =
    Source.queue[ScheduleTask](BUFFER_SIZE, OverflowStrategy.backpressure)
      .map(setTimestamp)
      .groupedWithin(GROUP_SIZE, TIME_WINDOW)
      .mapAsync(PARALLELISM)(sendBatch)
      .to(Sink.foreach(logCommandErrors))
      .run()

  // Make sure the command timestamp is set
  private def setTimestamp(cmd: ScheduleTask) =
    if (cmd.ts.isDefined) cmd else cmd.copy(ts = Some(System.currentTimeMillis()))

  // Send a command batch to the command manager
  private def sendBatch(batch: Seq[ScheduleTask]) =
    Future.sequence {
      batch.map { cmd ⇒
        (commandManager ? cmd).mapTo[CommandResponse]
      }
    }

  // Log command response errors
  private def logCommandErrors(responses: Seq[CommandResponse]) =
    responses.filter(_.responseType == ERROR).foreach { response ⇒
      sys.log.error(response.body)
    }

  // Helper function for parsing a `ttl` duration
  private def parseTTL(dur: String): Long = Duration(dur).toMillis

  // Helper function for parsing `ttw` values
  private def parseTTW(ttw: Option[String]): Seq[Long] = {
    val maybeWarnings: Option[Seq[Long]] = ttw.map(_.split(",").map(Duration(_).toMillis))
    maybeWarnings.getOrElse(Seq.empty)
  }

  // Helper function for parsing `tag` values
  private def parseTags(tags: Option[String]): Seq[String] = {
    val seq: Option[Seq[String]] = tags.map(_.split(",").toSeq)
    seq.getOrElse(Seq.empty)
  }

  // Schedule a task
  def scheduleTask(key: String, agg: String, ent: String, ttl: String, ttw: Option[String], tags: Option[String], ts: Option[Long]) =
    commandManager.ask(ScheduleTask(key, agg, ent, parseTTL(ttl), parseTTW(ttw), parseTags(tags), ts)).mapTo[CommandResponse]

  // Asynchronously schedule a task
  def scheduleTaskAsync(key: String, agg: String, ent: String, ttl: String, ttw: Option[String], tags: Option[String], ts: Option[Long]) =
    queue.offer(ScheduleTask(key, agg, ent, parseTTL(ttl), parseTTW(ttw), parseTags(tags), ts)).map {
      case QueueOfferResult.Enqueued ⇒ CommandResponse("", SUCCESS)
      case _ ⇒ CommandResponse("Buffer overflow: unable to queue task", ERROR)
    }

  // Complete a task
  def completeTask(key: String, agg: String, ent: String): Future[CommandResponse] =
    (commandManager ? CompleteTask(key, agg, ent)).mapTo[CommandResponse]

  // Get all active tasks for the given aggregate ID
  def queryAggregate(id: String): Future[Tasks] =
    (queryManager ? GetTasks(QueryType.AGGREGATE, aggregate = Some(id))).mapTo[Tasks]

  // Get all expired tasks for the given aggregate ID
  def queryAggregateExpirations(id: String): Future[Tasks] =
    (queryManager ? GetExpirations(QueryType.AGGREGATE, aggregate = Some(id))).mapTo[Tasks]

  // Get all task warnings for the given aggregate ID
  def queryAggregateWarnings(id: String): Future[Tasks] =
    (queryManager ? GetWarnings(QueryType.AGGREGATE, aggregate = Some(id))).mapTo[Tasks]

  // Count all scheduled tasks for the given aggregate ID
  def queryAggregateCount(id: String): Future[Count] =
    (queryManager ? GetCount(QueryType.AGGREGATE, aggregate = Some(id))).mapTo[Count]

  // Get all scheduled tasks for the given entity ID
  def queryEntity(id: String): Future[Tasks] =
    (queryManager ? GetTasks(QueryType.ENTITY, entity = Some(id))).mapTo[Tasks]

  // Get all expired tasks for the given entity ID
  def queryEntityExpirations(id: String): Future[Tasks] =
    (queryManager ? GetExpirations(QueryType.ENTITY, entity = Some(id))).mapTo[Tasks]

  // Get all task warnings for the given entity ID
  def queryEntityWarnings(id: String): Future[Tasks] =
    (queryManager ? GetWarnings(QueryType.ENTITY, entity = Some(id))).mapTo[Tasks]

  // Count all task warnings for the given entity ID
  def queryEntityCount(id: String): Future[Count] =
    (queryManager ? GetCount(QueryType.ENTITY, entity = Some(id))).mapTo[Count]

  // Get all scheduled tasks for the given key
  def queryKey(value: String): Future[Tasks] =
    (queryManager ? GetTasks(QueryType.KEY, key = Some(value))).mapTo[Tasks]

  // Count all scheduled tasks for the given key
  def queryKeyCount(value: String): Future[Count] =
    (queryManager ? GetCount(QueryType.KEY, key = Some(value))).mapTo[Count]

  // Get all expired tasks by tag within a given time window
  def queryExpiredTag(tag: String, window: String) =
    (queryManager ? GetTags(tag, window)).mapTo[Tasks]
}
