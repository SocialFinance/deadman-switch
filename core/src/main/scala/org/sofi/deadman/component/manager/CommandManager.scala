package org.sofi.deadman.component.manager

import akka.actor._
import com.rbmhtechnology.eventuate._
import org.sofi.deadman.component.actor.TaskActor
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.event._

final class CommandManager(val id: String, val eventLog: ActorRef) extends EventsourcedActor with ActorLogging {

  // Actor registry
  private var registry: Map[String, ActorRef] = Map.empty

  // Load an actor
  protected def actorFor(aggregate: String) =
    registry.get(aggregate) match {
      case Some(actor) ⇒ actor
      case None ⇒
        registry = registry + (aggregate -> context.actorOf(TaskActor.props(aggregate, id, eventLog)))
        registry(aggregate)
    }

  // Forward commands to aggregate specific components
  def onCommand: Receive = {
    case st: ScheduleTask ⇒ actorFor(st.aggregate) forward st
    case ct: CompleteTask ⇒ actorFor(ct.aggregate) forward ct
  }

  // Lazy load actors for non-expired task events
  def onEvent: Receive = {
    case Schedule(tasks) ⇒ tasks.filterNot(_.isExpired).foreach { t ⇒ val _ = actorFor(t.aggregate) }
    case t: Task ⇒ if (!t.isExpired) { val _ = actorFor(t.aggregate) }
  }
}

object CommandManager {
  def name(id: String): String = s"$id-task-manager"
  def props(id: String, eventLog: ActorRef): Props = Props(new CommandManager(id, eventLog))
}
