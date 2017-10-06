package org.sofi.deadman.component.counter

import akka.actor._
import com.rbmhtechnology.eventuate.EventsourcedView
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._

trait EventCounter extends EventsourcedView with ActorLogging {

  // Actor registry
  private var registry: Map[String, ActorRef] = Map.empty

  // Load an actor
  protected def actorFor(key: String) =
    registry.get(key) match {
      case Some(actor) ⇒ actor
      case None ⇒
        registry = registry + (key -> context.actorOf(CounterActor.props(key)))
        registry(key)
    }

  // Get scheduled task counts
  def onCommand = {
    case query: GetCount ⇒
      actorFor(query.queryKey) forward query
  }

  // Update counter service
  def onEvent = {
    case t: Task ⇒
      actorFor(taskKey(t)) ! CounterActor.Increment
    case t: TaskTermination ⇒
      actorFor(taskTerminationKey(t)) ! CounterActor.Decrement
    case TaskExpiration(t, _) ⇒
      actorFor(taskKey(t)) ! CounterActor.Decrement
  }

  // Determine the task query field
  def taskKey(t: Task): String

  // Determine the task termination query field
  def taskTerminationKey(t: TaskTermination): String
}
