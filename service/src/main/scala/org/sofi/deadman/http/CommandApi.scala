package org.sofi.deadman.http

import akka.actor._
import akka.pattern.ask
import akka.stream._
import akka.stream.scaladsl._
import akka.util.Timeout
import org.sofi.deadman.messages.command._, ResponseType._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

final class CommandApi(commandManager: ActorRef)(implicit val system: ActorSystem, val timeout: Timeout) {

  // Buffer size
  private val BUFFER_SIZE = 10000

  // Stream is chunked into groups of elements received within a time window
  private val GROUP_SIZE = 1000

  // Stream group time window
  private val TIME_WINDOW = 1.second

  // Make sure the command timestamp is set
  private def setTimestamp(task: ScheduleTask) =
    if (task.ts.isDefined) task else task.copy(ts = Some(System.currentTimeMillis()))

  // Perform some basic validation on a ScheduleTask command. This allows us to send commands
  // without waiting for a response from the command manager.
  private def validate(task: ScheduleTask): Either[Seq[String], ScheduleTask] = {
    var errors = Seq.empty[String]
    if (Try(task.aggregate.trim).getOrElse("").isEmpty) {
      errors = errors :+ s"Aggregate cannot be empty"
    }
    if (Try(task.entity.trim).getOrElse("").isEmpty) {
      errors = errors :+ s"Entity cannot be empty"
    }
    if (Try(task.key.trim).getOrElse("").isEmpty) {
      errors = errors :+ s"Key cannot be empty"
    }
    if (task.ttl < 1.second.toMillis) {
      errors = errors :+ s"Task ttl must be >= 1 second"
    }
    if (task.ttw.exists(_ < 1.second.toMillis)) {
      errors = errors :+ s"Task ttw must be >= 1 second"
    }
    if (task.ttw.exists(_ > task.ttl)) {
      errors = errors :+ s"Task ttw must be < ttl"
    }
    if (errors.nonEmpty) Left(errors) else Right(task)
  }

  // Send a batch to the command manager
  private def scheduleTasks(tasks: Seq[ScheduleTask]) =
    tasks.map { task ⇒
      validate(task) match {
        case Left(errors) ⇒ CommandResponse(ERROR, errors)
        case Right(_) ⇒
          commandManager ! task
          CommandResponse(QUEUED)
      }
    }

  // Log all command errors
  private def logErrors(responses: Seq[CommandResponse]) = {
    val (errors, _) = responses.partition(_.responseType == ERROR)
    if (errors.nonEmpty) errors.foreach(err ⇒ system.log.error(err.errors.mkString(",")))
    responses
  }

  // The following Akka Streams implementation batches writes to the command manager, buffering messages until the buffer size
  // is reached -or- a given amount of time passes. It also limits the number of outstanding asynchronous `ask` calls.
  val scheduleTaskFlow =
    Flow[ScheduleTask]
      .buffer(BUFFER_SIZE, OverflowStrategy.backpressure)
      .map(setTimestamp)
      .groupedWithin(GROUP_SIZE, TIME_WINDOW)
      .map(scheduleTasks)
      .map(logErrors)

  // Complete a task
  def completeTask(cmd: CompleteTask): Future[CommandResponse] =
    (commandManager ? cmd).mapTo[CommandResponse]
}
