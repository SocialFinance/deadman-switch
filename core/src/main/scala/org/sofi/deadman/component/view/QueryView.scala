package org.sofi.deadman.component.view

import akka.actor._
import com.rbmhtechnology.eventuate._
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._

private[view] trait QueryView extends EventsourcedView with ActorLogging {

  // Query ordering
  private val orderByTs = Ordering.by((t: Task) ⇒ t.ts + t.ttl)

  // View state
  private var state: Map[String, Set[Task]] = Map.empty

  // Query function
  private def query(key: String): Seq[Task] = state.getOrElse(key, Set.empty).toSeq.sorted(orderByTs)

  // Query the view state
  def onCommand: Receive = {
    case gt: GetTasks ⇒
      sender() ! Tasks(gt.queryKey.map(query).getOrElse(Seq.empty))
  }

  // Insert a task into the view state
  private def insertTask(t: Task): Unit = {
    val key = taskKey(t)
    val updated = state.getOrElse(key, Set.empty).filterNot(_.id == t.id) + t
    state = state + (key -> updated)
  }

  // Delete a task from the view state
  private def terminateTask(t: TaskTermination) = {
    val key = taskTerminationKey(t)
    val updated = state.getOrElse(key, Set.empty).filterNot(_.id == t.id)
    state = state + (key -> updated)
  }

  // Update the view state
  def onEvent: Receive = {
    case Schedule(tasks) ⇒ tasks.foreach(insertTask)
    case ScheduleTermination(terminations) ⇒ terminations.foreach(terminateTask)
    case t: Task ⇒ insertTask(t)
    case t: TaskTermination ⇒ terminateTask(t)
    case TaskExpiration(t, _) ⇒
      val key = taskKey(t)
      val updated = state.getOrElse(key, Set.empty).filterNot(_.id == t.id)
      state = state + (key -> updated)
  }

  // Determine the task query field
  def taskKey(t: Task): String

  // Determine the task termination query field
  def taskTerminationKey(t: TaskTermination): String
}
