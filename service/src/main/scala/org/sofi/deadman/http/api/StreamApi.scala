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

final class StreamApi(val id: String, val eventLog: ActorRef)(implicit system: ActorSystem, am: ActorMaterializer) {

  // Execution context
  private implicit val executionContext = system.dispatcher

  // Buffer size
  private val BUFFER_SIZE = 1000

  // Writes events to a log
  private val eventWriter = DurableEventWriter(s"$id-stream-api", eventLog)

  // Stream exception handler
  private def errorMessage[T]: PartialFunction[Throwable, Future[Either[String, T]]] = {
    case NonFatal(error) ⇒
      system.log.error("Stream API error: {}", error)
      Future.successful(Left(error.getMessage))
  }

  // Filter out valid schedule task commands and log validation errors
  private def createTaskEvent(result: ValidationResult[ScheduleTask]) = result match {
    case Invalid(nel) ⇒ throw new Exception(nel.map(_.error).toList.mkString(", "))
    case Valid(command) ⇒
      val t = command.event
      DurableEvent(payload = t, emitterAggregateId = Option(t.aggregate))
  }

  // Use the Eventuate Akka streams adapter to persist task events directly to an event log
  def scheduleTasks(source: Source[ScheduleRequest, NotUsed]): Future[Either[String, Tasks]] =
    source.buffer(BUFFER_SIZE, OverflowStrategy.backpressure)
      .map(_.ensureTimestamp)
      .map(_.validate)
      .map(createTaskEvent)
      .via(eventWriter)
      .map(_.payload.asInstanceOf[Task])
      .runFold(Seq.empty[Task])(_ :+ _)
      .map(tasks ⇒ Right(Tasks(tasks)))
      .recoverWith(errorMessage)

  // Filter out valid complete task commands and log validation errors
  private def createTaskTerminationEvent(result: ValidationResult[CompleteTask]) = result match {
    case Invalid(nel) ⇒ throw new Exception(nel.map(_.error).toList.mkString(", "))
    case Valid(command) ⇒
      val t = command.event
      DurableEvent(payload = t, emitterAggregateId = Option(t.aggregate))
  }

  // Use the Eventuate Akka streams adapter to persist task termination events directly to an event log
  def completeTasks(source: Source[CompleteRequest, NotUsed]): Future[Either[String, TaskTerminations]] =
    source.buffer(BUFFER_SIZE, OverflowStrategy.backpressure)
      .map(_.validate)
      .map(createTaskTerminationEvent)
      .via(eventWriter)
      .map(_.payload.asInstanceOf[TaskTermination])
      .runFold(Seq.empty[TaskTermination])((a, t) ⇒ a :+ t)
      .map(t ⇒ Right(TaskTerminations(t)))
      .recoverWith(errorMessage)
}
