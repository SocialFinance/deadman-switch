package org.sofi.deadman.component.actor

import akka.actor._
import com.rbmhtechnology.eventuate._
import org.sofi.deadman.messages.command._, ResponseType._
import org.sofi.deadman.messages.event._
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

final class TaskActor(val aggregate: String, val replica: String, val eventLog: ActorRef) extends EventsourcedActor with ActorLogging {

  // Implicit execution context
  import context.dispatcher

  // Actor ID
  override val id = s"$aggregate-$replica"

  // Aggregate ID
  override val aggregateId = Some(aggregate)

  // Schedules task expiration and warning checks
  private val _ = context.system.scheduler.schedule(500.milliseconds, 500.milliseconds, self, TaskActor.Tick)

  // Pending task expiration commands
  private var tasks: Map[String, ExpireTask] = Map.empty

  // Active warning commands
  private var warnings: Map[String, Seq[IssueTaskWarning]] = Map.empty

  // Cancel a task
  private def cancel(id: String): Unit = {
    tasks = tasks - id
    warnings = warnings - id
  }

  // Schedule expiration and warning commands
  private def schedule(t: Task): Unit = {
    val id = t.id
    tasks = tasks + (id -> ExpireTask(t))
    val warns = t.ttw.map(ttw ⇒ IssueTaskWarning(t, ttw))
    warnings = warnings + (id -> warns)
  }

  // Check for expired tasks and send commands
  private def checkExpired(): Unit = {
    val (expired, nonExpired) = tasks.partition(_._2.task.isExpired)
    tasks = nonExpired
    expired.foreach {
      case (_, expireTask) ⇒
        self ! expireTask
    }
  }

  // Check for expired warnings and send commands
  private def checkWarnings(): Unit =
    warnings.map {
      case (wid, warns) ⇒
        val (nonExpired, expired) = warns.partition(w ⇒ w.task.ts + w.ttw >= System.currentTimeMillis())
        expired.foreach(cmd ⇒ self ! cmd)
        (wid, nonExpired)
    } foreach {
      case (wid, nonExpired) ⇒
        warnings = warnings + (wid -> nonExpired)
    }

  // Process warnings and expirations
  private def onTick(): Unit = {
    checkWarnings()
    checkExpired()
  }

  // Create and persist a Task event
  private def persistTask(event: Task): Unit = {
    log.info(s"Persisting Task: ${event.id}")
    persist(event) {
      case Success(_) ⇒ sender() ! CommandResponse(SUCCESS)
      case Failure(err) ⇒
        log.error("Unable to persist task {}", err)
        sender() ! CommandResponse(ERROR, Seq(err.getMessage))
    }
  }

  // Create and persist a TaskTermination event
  private def persistTaskTermination(event: TaskTermination): Unit = {
    if (!tasks.contains(event.id)) {
      log.warning("Task not found: {}", event.id)
    } else {
      log.info(s"Completing Task: ${event.id}")
      persist(event) {
        case Success(_) ⇒ sender() ! CommandResponse(SUCCESS)
        case Failure(err) ⇒
          log.error("Unable to persist task termination {}", err)
          sender() ! CommandResponse(ERROR, Seq(err.getMessage))
      }
    }
  }

  // Create and persist a TaskExpiration event
  private def persistTaskExpiration(task: Task): Unit =
    if (task.isExpired) {
      persist(TaskExpiration(task, System.currentTimeMillis())) {
        case Success(_) ⇒ log.info("Expiration for task: {}", task)
        case Failure(err) ⇒ log.error("Unable to persist task expiration {}", err)
      }
    }

  // Create and persist a TaskWarning event
  private def persistTaskWarning(task: Task, ttw: Long) =
    if (!task.isExpired) {
      persist(TaskWarning(task, ttw, System.currentTimeMillis())) {
        case Success(_) ⇒ log.info("Warning for task: {}", task)
        case Failure(err) ⇒ log.error("Unable to persist task expiration warning {}", err)
      }
    }

  // Create persistent events when a command is received
  def onCommand: Receive = {
    case cmd: ScheduleTask ⇒ persistTask(cmd.event)
    case cmd: CompleteTask ⇒ persistTaskTermination(cmd.event)
    case ExpireTask(task) ⇒ persistTaskExpiration(task)
    case IssueTaskWarning(task, ttw) ⇒ persistTaskWarning(task, ttw)
    case TaskActor.Tick ⇒ onTick()
  }

  // Schedule or cancel expiration timers
  def onEvent: Receive = {
    case t: Task ⇒ if (!t.isExpired) schedule(t)
    case t: TaskTermination ⇒ cancel(t.id)
  }
}

object TaskActor {
  case object Tick
  def props(aggregate: String, replica: String, eventLog: ActorRef): Props = Props(new TaskActor(aggregate, replica, eventLog))
}
