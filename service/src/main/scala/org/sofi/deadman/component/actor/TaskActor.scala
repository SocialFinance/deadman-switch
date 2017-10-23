package org.sofi.deadman.component.actor

import akka.actor._
import com.rbmhtechnology.eventuate._
import org.sofi.deadman.messages.command._, ResponseType._
import org.sofi.deadman.messages.event._
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

final class TaskActor(val aggregate: String, val replica: String, val eventLog: ActorRef) extends EventsourcedActor with ActorLogging {

  // Max number of tasks
  private val MAX_TASKS = 1000

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

  // Create persistent events when a command is received
  def onCommand: Receive = {
    case ScheduleTask(key, `aggregate`, entity, ttl, ttw, tags, ts) ⇒
      val id = uid(aggregate, entity, key)
      if (tasks.keys.size >= MAX_TASKS) {
        sender() ! CommandResponse(ERROR, Seq(s"Aggregate $aggregate does not support > $MAX_TASKS tasks"))
      } else {
        log.info(s"Persisting Task: $id")
        persist(Task(key, aggregate, entity, ts.getOrElse(System.currentTimeMillis()), ttl, ttw, tags)) {
          case Success(_) ⇒ sender() ! CommandResponse(SUCCESS)
          case Failure(err) ⇒
            log.error("Unable to persist task {}", err)
            sender() ! CommandResponse(ERROR, Seq(err.getMessage))
        }
      }
    case CompleteTask(key, `aggregate`, entity) ⇒
      val id = uid(aggregate, entity, key)
      if (!tasks.contains(id)) {
        sender() ! CommandResponse(ERROR, Seq(s"Task not found: $id"))
      } else {
        persist(TaskTermination(key, aggregate, entity)) {
          case Success(_) ⇒ sender() ! CommandResponse(SUCCESS)
          case Failure(err) ⇒
            log.error("Unable to persist task termination {}", err)
            sender() ! CommandResponse(ERROR, Seq(err.getMessage))
        }
      }
    case ExpireTask(task) ⇒
      if (task.isExpired) {
        persist(TaskExpiration(task, System.currentTimeMillis())) {
          case Success(_) ⇒ log.info("Expiration for task: {}", task)
          case Failure(err) ⇒ log.error("Unable to persist task expiration {}", err)
        }
      }
    case IssueTaskWarning(task, ttw) ⇒
      if (!task.isExpired) {
        persist(TaskWarning(task, ttw, System.currentTimeMillis())) {
          case Success(_) ⇒ log.info("Warning for task: {}", task)
          case Failure(err) ⇒ log.error("Unable to persist task expiration warning {}", err)
        }
      }
    case TaskActor.Tick ⇒
      checkWarnings()
      checkExpired()
  }

  // Schedule or cancel expiration timers
  def onEvent: Receive = {
    case t: Task ⇒
      schedule(t)
    case t: TaskTermination ⇒
      cancel(t.id)
  }
}

object TaskActor {
  case object Tick
  def props(aggregate: String, replica: String, eventLog: ActorRef): Props =
    Props(new TaskActor(aggregate, replica, eventLog))
}
