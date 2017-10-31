package org.sofi.deadman.test.event

import akka.actor._
import com.rbmhtechnology.eventuate.EventsourcedView
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.event._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class TaskExpirationTest extends TestSystem {
  // Helper view that forwards a `TaskExpiration` event back to the test actor for assertion
  final class TaskExpirationForwarder(val id: String, val eventLog: ActorRef) extends EventsourcedView {
    def onCommand = { case _ ⇒ }
    def onEvent = {
      case event: TaskExpiration ⇒
        testActor ! event
    }
  }
  "A task actor" must {
    "Successfully persist a task expiration event" in {
      // Forwards task expiration events to the test actor
      system.actorOf(Props(new TaskExpirationForwarder(aggregate, eventLog)))
      taskActor ! ScheduleTask("test", aggregate, "0", 1.second.toMillis)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      expectMsgPF() {
        case event: TaskExpiration ⇒
          event.task.key must be("test")
          event.task.aggregate must be(aggregate)
          event.task.entity must be("0")
          event.task.ttl must be(1.second.toMillis)
      }
    }
  }
}
