package org.sofi.deadman.http.api

import akka.NotUsed
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import cats.data.Validated._
import com.rbmhtechnology.eventuate.DurableEvent
import com.rbmhtechnology.eventuate.adapter.stream.DurableEventWriter
import org.sofi.deadman.http.request._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.messages.validation._
import scala.concurrent.Future
import scala.util.control.NonFatal

final class StreamApi(val eventLog: ActorRef)(implicit system: ActorSystem, am: ActorMaterializer) {

  // Execution context
  private implicit val executionContext = system.dispatcher

  // Buffer size
  private val BUFFER_SIZE = 1000

  // Writer ID
  private val WRITER_ID = "deadman-stream-api-v1"

  // Make sure the command timestamp is set on a schedule request
  private def setTimestamp(r: ScheduleRequest): ScheduleRequest =
    if (r.ts.isDefined) r else r.copy(ts = Some(System.currentTimeMillis()))

  // Run validation on a schedule request
  private def validateRequest(r: ScheduleRequest): ValidationResult[ScheduleTask] =
    validate(r.key, r.aggregate, r.entity, r.ttl, r.ttw, r.tags, r.ts)

  // Run validation on a sequence of complete requests
  private def validateCompleteRequest(r: CompleteRequest) =
    validateCompletion(r.key, r.aggregate, r.entity)

  // Filter out valid schedule task commands and log validation errors
  private def createEvent(result: ValidationResult[ScheduleTask]): Task =
    result match {
      case Valid(command) ⇒ command.event
      case Invalid(nel) ⇒ throw new Exception(nel.map(_.error).toList.mkString(", "))
    }

  // Filter out valid complete task commands and log validation errors
  private def createCompleteEvent(result: ValidationResult[CompleteTask]) =
    result match {
      case Valid(command) ⇒ command.event
      case Invalid(nel) ⇒ throw new Exception(nel.map(_.error).toList.mkString(", "))
    }

  // Create a durable event from a task sequence. NOTE: Assumes all events have the same aggregate
  private def createDurableEvent(t: Task): DurableEvent =
    DurableEvent(payload = t, emitterAggregateId = Option(t.aggregate))

  // Create a durable event from a task termination sequence. NOTE: Assumes all events have the same aggregate
  private def createTerminationDurableEvent(t: TaskTermination) =
    DurableEvent(payload = t, emitterAggregateId = Option(t.aggregate))

  // Stream exception handler
  def errorMessage[T]: PartialFunction[Throwable, Future[Either[String, T]]] = {
    case NonFatal(error) ⇒
      system.log.error("Stream API error: {}", error)
      Future.successful(Left(error.getMessage))
  }

  // Use the Eventuate Akka streams adapter to persist task events directly to an event log
  def scheduleTasks(source: Source[ScheduleRequest, NotUsed]): Future[Either[String, Tasks]] =
    source.buffer(BUFFER_SIZE, OverflowStrategy.backpressure)
      .map(setTimestamp)
      .map(validateRequest)
      .map(createEvent)
      .map(createDurableEvent)
      .via(DurableEventWriter(WRITER_ID, eventLog))
      .map(_.payload.asInstanceOf[Task])
      .runFold(Seq.empty[Task])((a, t) ⇒ a :+ t)
      .map(tasks => Right(Tasks(tasks)))
      .recoverWith(errorMessage)

  // Use the Eventuate Akka streams adapter to persist task termination events directly to an event log
  def completeTasks(source: Source[CompleteRequest, NotUsed]): Future[Either[String, Seq[TaskTermination]]] =
    source.buffer(BUFFER_SIZE, OverflowStrategy.backpressure)
      .map(validateCompleteRequest)
      .map(createCompleteEvent)
      .map(createTerminationDurableEvent)
      .via(DurableEventWriter(WRITER_ID, eventLog))
      .map(_.payload.asInstanceOf[TaskTermination])
      .runFold(Seq.empty[TaskTermination])((a, t) ⇒ a :+ t)
      .map(Right(_))
      .recoverWith(errorMessage)
}
