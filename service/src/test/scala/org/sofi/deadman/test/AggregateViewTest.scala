package org.sofi.deadman.test

import org.sofi.deadman.component.view._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._

final class AggregateViewTest extends TestSystem {

  // View
  val viewActor = system.actorOf(AggregateView.props(aggregate, eventLog))

  "An aggregate view" must {
    "Successfully receive a Task event" in {
      taskActor ! ScheduleTask("test", aggregate, "0", 100L)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      viewActor ! GetTasks(GetTasks.ViewType.AGGREGATE, aggregate = Some(aggregate))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.size must be(1)
          result.tasks.foreach(_.aggregate must be(aggregate))
      }
    }
    "Successfully clear state on a TaskExpiration event" in {
      // Wait for task to expire
      Thread.sleep(1100L)
      // Query view state
      viewActor ! GetTasks(GetTasks.ViewType.AGGREGATE, aggregate = Some(aggregate))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.isEmpty must be(true)
      }
    }
  }
}
