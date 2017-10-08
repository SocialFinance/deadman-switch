package org.sofi.deadman.test

import akka.actor._
import com.rbmhtechnology.eventuate.EventsourcedView
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.event._

final class TaskWarningTest extends TestSystem {
  // Helper view that forwards a `TaskWarning` event back to the test actor for assertion
  final class TaskWarningForwarder(val id: String, val eventLog: ActorRef) extends EventsourcedView {
    def onCommand = { case _ ⇒ }
    def onEvent = {
      case event: TaskWarning ⇒
        testActor ! event
    }
  }
  "A task actor" must {
    "Successfully persist a task warning event" in {
      system.actorOf(Props(new TaskWarningForwarder(aggregate, eventLog)))
      taskActor ! ScheduleTask("test", aggregate, "0", 1000000L, Seq(1997L))
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      expectMsgPF() {
        case event: TaskWarning ⇒
          event.ts must be(1997L)
          event.task.key must be("test")
          event.task.aggregate must be(aggregate)
          event.task.entity must be("0")
      }
    }
  }
}
