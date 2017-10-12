package org.sofi.deadman.test.view

import org.sofi.deadman.component.view._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class KeyViewTest extends TestSystem {

  // View
  val viewActor = system.actorOf(KeyView.props(aggregate, eventLog))

  "A key view" must {
    "Successfully receive Task events" in {
      // Should come back in query results
      taskActor ! ScheduleTask("test", aggregate, "0", 1.second.toMillis)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      // Should come back in query results
      taskActor ! ScheduleTask("test", aggregate, "1", 1.second.toMillis)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      // Should NOT come back in query results
      taskActor ! ScheduleTask("test2", aggregate, "2", 1.second.toMillis)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      // Query
      viewActor ! GetTasks(QueryType.KEY, key = Some("test"))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.size must be(2)
          result.tasks.foreach(_.key must be("test"))
      }
    }
    "Successfully clear state on a TaskExpiration event" in {
      // Wait for tasks to expire
      Thread.sleep(2.seconds.toMillis)
      // Query view state
      viewActor ! GetTasks(QueryType.KEY, key = Some("test"))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.isEmpty must be(true)
      }
    }
  }
}