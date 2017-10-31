package org.sofi.deadman.test.view

import org.sofi.deadman.component.view._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class AggregateViewTest extends TestSystem {

  // View
  private val viewActor = system.actorOf(AggregateView.props(aggregate, eventLog))

  "An aggregate view" must {
    "Successfully receive a Task event" in {
      taskActor ! ScheduleTask("test", aggregate, "0", 1.second.toMillis)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      viewActor ! GetTasks(QueryType.AGGREGATE, aggregate = Some(aggregate))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.size must be(1)
          result.tasks.foreach(_.aggregate must be(aggregate))
      }
    }
    "Successfully clear state on a TaskExpiration event" in {
      // Wait for task to expire
      Thread.sleep(2.seconds.toMillis)
      // Query view state
      viewActor ! GetTasks(QueryType.AGGREGATE, aggregate = Some(aggregate))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.isEmpty must be(true)
      }
    }
  }
}
