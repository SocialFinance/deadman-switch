package org.sofi.deadman.http

import akka.actor._
import akka.pattern.ask
import akka.stream._
import akka.stream.scaladsl._
import akka.util.Timeout
import org.sofi.deadman.messages.command._, ResponseType._
import org.sofi.deadman.messages.query._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.control.NonFatal

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
  private val PARALLELISM = 10

  // Make sure the command timestamp is set
  private def setTimestamp(cmd: ScheduleTask) =
    if (cmd.ts.isDefined) cmd else cmd.copy(ts = Some(System.currentTimeMillis()))

  // Send a batch to the command manager
  private def sendCommands(commands: Seq[ScheduleTask]) =
    Future.sequence {
      commands.map { cmd ⇒
        (commandManager ? cmd).mapTo[CommandResponse]
      }
    }

  // Filter and log command errors
  private def filterErrors(responses: Seq[CommandResponse]) = {
    val (errors, _) = responses.partition(_.responseType == ERROR)
    if (errors.nonEmpty) errors.foreach(err ⇒ sys.log.error(err.body))
    errors
  }

  // The following Akka Streams implementation batches writes to the command manager, buffering messages until the buffer size
  // is reached -or- a given amount of time passes. It also limits the number of outstanding asynchronous `ask` calls.
  val scheduleTaskFlow =
    Flow[ScheduleTask]
      .map(setTimestamp)
      .groupedWithin(GROUP_SIZE, TIME_WINDOW)
      .mapAsync(PARALLELISM)(sendCommands)
      .map(filterErrors)

  // Use a queue-backed source in front of our Akka streams flow
  private val queue =
    Source.queue[ScheduleTask](BUFFER_SIZE, OverflowStrategy.backpressure)
      .via(scheduleTaskFlow)
      .to(Sink.ignore)
      .run()

  // Send a command response error when a task cannot be queued
  private val notQueued: PartialFunction[Throwable, Future[CommandResponse]] = {
    case NonFatal(err) ⇒
      sys.log.error("Task queue error: {}", err)
      Future.successful(CommandResponse("Buffer overflow: unable to queue task", ERROR))
  }

  // Offer at ask to the akka streams queue
  def queueTask(task: ScheduleTask): Future[CommandResponse] =
    queue.offer(task).map {
      case QueueOfferResult.Enqueued ⇒ CommandResponse("", QUEUED)
      case _ ⇒ CommandResponse("Buffer overflow: unable to queue task", ERROR)
    } recoverWith notQueued

  // Complete a task
  def completeTask(completeTask: CompleteTask): Future[CommandResponse] =
    (commandManager ? completeTask).mapTo[CommandResponse]

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
