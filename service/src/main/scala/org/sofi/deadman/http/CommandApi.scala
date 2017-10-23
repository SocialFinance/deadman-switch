package org.sofi.deadman.http

import akka.actor._
import akka.pattern.ask
import akka.stream._
import akka.stream.scaladsl._
import akka.util.Timeout
import cats.data.Validated._
import org.sofi.deadman.messages.command._, ResponseType._
import org.sofi.deadman.messages.validation._
import scala.concurrent.Future
import scala.concurrent.duration._

final class CommandApi(commandManager: ActorRef)(implicit val system: ActorSystem, val timeout: Timeout) {

  // Buffer size
  private val BUFFER_SIZE = 10000

  // Stream is chunked into groups of elements received within a time window
  private val GROUP_SIZE = 1000

  // Stream group time window
  private val TIME_WINDOW = 1.second

  // Make sure the command timestamp is set
  private def setTimestamp(req: ScheduleRequest) =
    if (req.ts.isDefined) req else req.copy(ts = Some(System.currentTimeMillis()))

  // Send a batch of ScheduleTask commands to the command manager
  private def scheduleTasks(requests: Seq[ScheduleRequest]) =
    requests.map { r ⇒
      validate(r.key, r.aggregate, r.entity, r.ttl, r.ttw, r.tags, r.ts) match {
        case Invalid(nel) ⇒ CommandResponse(ERROR, nel.map(_.error).toList)
        case Valid(task) ⇒
          commandManager ! task
          CommandResponse(QUEUED)
      }
    }

  // Log all command errors
  private def logErrors(reps: Seq[CommandResponse]) = {
    reps.foreach { rep ⇒
      if (rep.responseType == ERROR) {
        system.log.error(rep.errors.mkString(","))
      }
    }
    reps
  }

  // The following Akka Streams implementation batches writes to the command manager, buffering messages until the buffer size
  // is reached -or- a given amount of time passes. It also limits the number of outstanding asynchronous `ask` calls.
  val scheduleTaskFlow =
    Flow[ScheduleRequest]
      .buffer(BUFFER_SIZE, OverflowStrategy.backpressure)
      .map(setTimestamp)
      .groupedWithin(GROUP_SIZE, TIME_WINDOW)
      .map(scheduleTasks)
      .map(logErrors)

  // Complete a task
  def completeTask(req: CompleteRequest): Future[CommandResponse] =
    validateCompletion(req.key, req.aggregate, req.entity) match {
      case Invalid(nel) ⇒ Future.successful(CommandResponse(ERROR, nel.map(_.error).toList))
      case Valid(command) ⇒ (commandManager ? command).mapTo[CommandResponse]
    }
}
