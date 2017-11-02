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
import scala.concurrent.duration._

final class StreamApi(val eventLog: ActorRef)(implicit system: ActorSystem, am: ActorMaterializer) {

  // Execution context
  private implicit val executionContext = system.dispatcher

  // Buffer size
  private val BUFFER_SIZE = 1000

  // Writer ID
  private val WRITER_ID = "deadman-stream-api-v1"

  // Stream is chunked into groups of elements received within a time window
  private val GROUP_SIZE = 10

  // Stream group time window
  private val TIME_WINDOW = 1.second

  // Make sure the command timestamp is set
  private def setTimestamps(requests: Seq[ScheduleRequest]) =
    requests.map(r ⇒ if (r.ts.isDefined) r else r.copy(ts = Some(System.currentTimeMillis())))

  // Run validation on a sequence of schedule requests
  private def validateRequests(requests: Seq[ScheduleRequest]) =
    requests.map(r ⇒ validate(r.key, r.aggregate, r.entity, r.ttl, r.ttw, r.tags, r.ts))

  // Run validation on a sequence of complete requests
  private def validateCompleteRequests(requests: Seq[CompleteRequest]) =
    requests.map(r ⇒ validateCompletion(r.key, r.aggregate, r.entity))

  // Filter out valid schedule task commands and log validation errors
  private def createEvents(results: Seq[ValidationResult[ScheduleTask]]) = results.flatMap {
    case Valid(command) ⇒ Some(command.event)
    case Invalid(nel) ⇒
      nel.toList.foreach(dv ⇒ system.log.error(dv.error))
      None
  }

  // Filter out valid complete task commands and log validation errors
  private def createCompleteEvents(results: Seq[ValidationResult[CompleteTask]]) = results.flatMap {
    case Valid(command) ⇒ Some(command.event)
    case Invalid(nel) ⇒
      nel.toList.foreach(dv ⇒ system.log.error(dv.error))
      None
  }

  // Create a durable event from a task sequence. NOTE: Assumes all events have the same aggregate
  private def createDurableEvent(tasks: Seq[Task]) = {
    system.log.info("Creating durable event with {} tasks", tasks.size)
    DurableEvent(
      payload = Schedule(tasks),
      emitterAggregateId = tasks.headOption.map(_.aggregate)
    )
  }

  // Create a durable event from a task termination sequence. NOTE: Assumes all events have the same aggregate
  private def createTerminationDurableEvent(terminations: Seq[TaskTermination]) = DurableEvent(
    payload = ScheduleTermination(terminations),
    emitterAggregateId = terminations.headOption.map(_.aggregate)
  )

  // Use the Eventuate Akka streams adapter to persist task events directly to an event log
  def scheduleTasks(source: Source[ScheduleRequest, NotUsed]): Future[Tasks] =
    source.buffer(BUFFER_SIZE, OverflowStrategy.backpressure)
      .groupedWithin(GROUP_SIZE, TIME_WINDOW)
      .map(setTimestamps)
      .map(validateRequests)
      .map(createEvents)
      .map(createDurableEvent)
      .via(DurableEventWriter(WRITER_ID, eventLog))
      .map(_.payload.asInstanceOf[Schedule])
      .runFold(Seq.empty[Task])((a, s) ⇒ a ++ s.tasks)
      .map(Tasks(_))

  // Use the Eventuate Akka streams adapter to persist task termination events directly to an event log
  def completeTasks(source: Source[CompleteRequest, NotUsed]): Future[Int] =
    source.buffer(BUFFER_SIZE, OverflowStrategy.backpressure)
      .groupedWithin(GROUP_SIZE, TIME_WINDOW)
      .map(validateCompleteRequests)
      .map(createCompleteEvents)
      .map(createTerminationDurableEvent)
      .via(DurableEventWriter(WRITER_ID, eventLog))
      .map(_.payload.asInstanceOf[ScheduleTermination])
      .runFold(0)((a, s) ⇒ a + s.terminations.size)
}
