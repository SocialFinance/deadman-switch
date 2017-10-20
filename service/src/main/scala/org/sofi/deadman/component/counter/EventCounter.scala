package org.sofi.deadman.component.counter

import akka.actor._
import com.rbmhtechnology.eventuate.EventsourcedView
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._

object Counter {
  trait Evt
  case object Incremented extends Evt
  case object Decremented extends Evt

  case class CounterState(count: Long) {
    def updated(cmd: Evt): CounterState = {
      cmd match {
        case Incremented ⇒ copy(count + 1)
        case Decremented ⇒ copy(count - 1)
      }
    }
  }
}

trait EventCounter extends EventsourcedView with ActorLogging {

  // CounterState registry
  private var registry: Map[String, Counter.CounterState] = Map.empty

  private def stateForKey(key: String) = registry.getOrElse(key, Counter.CounterState(0))

  private def updateStateForKey(key: String, evt: Counter.Evt) =
    registry = registry + (key -> stateForKey(key).updated(evt))

  // Get scheduled task counts
  def onCommand = {
    case query: GetCount ⇒
      sender() ! Count(stateForKey(query.queryKey).count)
  }

  // Update counter service
  def onEvent = {
    case t: Task ⇒
      updateStateForKey(taskKey(t), Counter.Incremented)
    case t: TaskTermination ⇒
      updateStateForKey(taskTerminationKey(t), Counter.Decremented)
    case TaskExpiration(t, _) ⇒
      updateStateForKey(taskKey(t), Counter.Decremented)
  }

  // Determine the task query field
  def taskKey(t: Task): String

  // Determine the task termination query field
  def taskTerminationKey(t: TaskTermination): String
}
