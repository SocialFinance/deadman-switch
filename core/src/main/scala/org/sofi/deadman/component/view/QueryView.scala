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

  // Update the view state
  def onEvent: Receive = {
    case t: Task ⇒
      val key = taskKey(t)
      val updated = state.getOrElse(key, Set.empty).filterNot(_.id == t.id) + t
      state = state + (key -> updated)
    case t: TaskTermination ⇒
      val key = taskTerminationKey(t)
      val updated = state.getOrElse(key, Set.empty).filterNot(_.id == t.id)
      state = state + (key -> updated)
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
