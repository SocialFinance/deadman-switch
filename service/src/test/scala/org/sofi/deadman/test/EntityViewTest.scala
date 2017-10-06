package org.sofi.deadman.test

import org.sofi.deadman.component.view._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._

final class EntityViewTest extends TestSystem {

  // View
  val viewActor = system.actorOf(EntityView.props(aggregate, eventLog))

  "An entity view" must {
    "Successfully receive Task events" in {
      // Should come back in query results
      taskActor ! ScheduleTask("test1", aggregate, "0", 100L)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      // Should come back in query results
      taskActor ! ScheduleTask("test2", aggregate, "0", 100L)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      // Should NOT come back in query results
      taskActor ! ScheduleTask("test3", aggregate, "1", 100L)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      // Query
      viewActor ! GetTasks(QueryType.ENTITY, entity = Some("0"))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.size must be(2)
          result.tasks.foreach(_.entity must be("0"))
      }
    }
    "Successfully clear state on a TaskExpiration event" in {
      // Wait for tasks to expire
      Thread.sleep(1100L)
      // Query view state
      viewActor ! GetTasks(QueryType.ENTITY, entity = Some("0"))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.isEmpty must be(true)
      }
    }
  }
}
