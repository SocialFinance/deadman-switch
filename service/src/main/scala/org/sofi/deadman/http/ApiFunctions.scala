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

final class ApiFunctions(commandManager: ActorRef, queryManager: ActorRef)(implicit val sys: ActorSystem, val t: Timeout, val am: ActorMaterializer) {

  // Stream is chunked into groups of elements received within a time window
  private val GROUP_SIZE = 1000

  // Stream group time window
  private val TIME_WINDOW = 1.second

  // Stream parallelism setting
  private val PARALLELISM = 10

  // Make sure the command timestamp is set
  private def setTimestamp(cmd: ScheduleTask) =
    if (cmd.ts.isDefined) cmd else cmd.copy(ts = Some(System.currentTimeMillis()))

  // Perform some basic validation on a ScheduleTask command. This allows us to send commands
  // without waiting for a response from the command manager.
  def validate(st: ScheduleTask): Seq[String] = {
    var errors = Seq.empty[String]
    if (st.ttl < 1.second.toMillis) {
      errors = errors :+ s"Task ttl must be >= 1 second"
    }
    if (st.ttw.exists(_ < 1.second.toMillis)) {
      errors = errors :+ s"Task ttw must be >= 1 second"
    }
    if (st.ttw.exists(_ > st.ttl)) {
      errors = errors :+ s"Task ttw must be < ttl"
    }
    errors
  }

  // Send a batch to the command manager
  private def sendCommands(commands: Seq[ScheduleTask]) =
    Future.successful {
      commands.map { cmd ⇒
        val errors = validate(cmd)
        if (errors.nonEmpty) CommandResponse(errors.mkString(","), ERROR) else {
          commandManager ! cmd
          CommandResponse("", SUCCESS)
        }
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
  val scheduleTask =
    Flow[ScheduleTask]
      .map(setTimestamp)
      .groupedWithin(GROUP_SIZE, TIME_WINDOW)
      .mapAsync(PARALLELISM)(sendCommands)
      .map(filterErrors)

  // Complete a task
  def completeTask(cmd: CompleteTask): Future[CommandResponse] =
    (commandManager ? cmd).mapTo[CommandResponse]

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
