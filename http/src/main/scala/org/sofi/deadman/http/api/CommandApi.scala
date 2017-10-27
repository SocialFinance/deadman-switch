package org.sofi.deadman.http.api

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.util.Timeout
import cats.data.Validated._
import org.sofi.deadman.http.request._
import org.sofi.deadman.messages.command._, ResponseType._
import org.sofi.deadman.messages.validation._
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

  // Send a command to the command manager
  private def sendCommand(command: Any) = {
    commandManager ! command
    CommandResponse(QUEUED)
  }

  // Validate, create and send a batch of ScheduleTask commands to the command manager
  private def scheduleTasks(requests: Seq[ScheduleRequest]) =
    requests.map { r ⇒
      validate(r.key, r.aggregate, r.entity, r.ttl, r.ttw, r.tags, r.ts) match {
        case Invalid(nel) ⇒ CommandResponse(ERROR, nel.map(_.error).toList)
        case Valid(command) ⇒ sendCommand(command)
      }
    }

  // Validate, create and send a batch of CompleteTask commands to the command manager
  private def completeTasks(requests: Seq[CompleteRequest]) =
    requests.map { r ⇒
      validateCompletion(r.key, r.aggregate, r.entity) match {
        case Invalid(nel) ⇒ CommandResponse(ERROR, nel.map(_.error).toList)
        case Valid(command) ⇒ sendCommand(command)
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

  // The following Akka Streams implementations batch writes to the command manager, buffering messages until the buffer size
  // is reached -or- a given amount of time passes.

  // Task scheduling flow
  val scheduleTaskFlow =
    Flow[ScheduleRequest]
      .buffer(BUFFER_SIZE, OverflowStrategy.backpressure)
      .map(setTimestamp)
      .groupedWithin(GROUP_SIZE, TIME_WINDOW)
      .map(scheduleTasks)
      .map(logErrors)

  // Task completion flow
  val completeTaskFlow =
    Flow[CompleteRequest]
      .buffer(BUFFER_SIZE, OverflowStrategy.backpressure)
      .groupedWithin(GROUP_SIZE, TIME_WINDOW)
      .map(completeTasks)
      .map(logErrors)
}
