package org.sofi.deadman.test.event

import akka.actor._
import com.rbmhtechnology.eventuate.EventsourcedView
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.event._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class TaskTerminationTest extends TestSystem {
  // Helper view that forwards a `TaskTermination` event back to the test actor for assertion
  final class TaskTerminationForwarder(val id: String, val eventLog: ActorRef) extends EventsourcedView {
    def onCommand = { case _ ⇒ }
    def onEvent = {
      case event: TaskTermination ⇒
        testActor ! event
    }
  }
  "A task actor" must {
    "Successfully persist a task termination event" in {
      system.actorOf(Props(new TaskTerminationForwarder(aggregate, eventLog)))
      taskActor ! ScheduleTask("test", aggregate, "0", 10.days.toMillis)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      taskActor ! CompleteTask("test", aggregate, "0")
      // TODO: Why don't we get the command response?
      expectMsg(TaskTermination("test", aggregate, "0"))
    }
  }
}
