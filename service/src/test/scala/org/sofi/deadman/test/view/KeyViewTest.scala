package org.sofi.deadman.test.view

import org.sofi.deadman.component.view._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class KeyViewTest extends TestSystem {

  // View
  private val viewActor = system.actorOf(KeyView.props(aggregate, eventLog))

  // Test TTL (min allowed value)
  private val ttl = 1.second.toMillis

  // Expected value
  private val taskKey = "test"

  "A key view" must {
    "Successfully receive Task events" in {
      // Should come back in query results
      taskActor ! ScheduleTask(taskKey, aggregate, "0", ttl)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      // Should come back in query results
      taskActor ! ScheduleTask(taskKey, aggregate, "1", ttl)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      // Should NOT come back in query results
      taskActor ! ScheduleTask("test2", aggregate, "2", ttl)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      // Query
      viewActor ! GetTasks(QueryType.KEY, key = Some(taskKey))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.size must be(2)
          result.tasks.foreach(_.key must be(taskKey))
      }
    }
    "Successfully clear state on a TaskExpiration event" in {
      // Wait for tasks to expire
      Thread.sleep(2.seconds.toMillis)
      // Query view state
      viewActor ! GetTasks(QueryType.KEY, key = Some(taskKey))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.isEmpty must be(true)
      }
    }
  }
}
