package org.sofi.deadman.test.view

import org.sofi.deadman.component.view._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class EntityViewTest extends TestSystem {

  // View
  private val viewActor = system.actorOf(EntityView.props(aggregate, eventLog))

  // Test TTL (min allowed value)
  private val ttl = 1.second.toMillis

  // Expected value
  private val entity = "0"

  "An entity view" must {
    "Successfully receive Task events" in {
      // Should come back in query results
      taskActor ! ScheduleTask("test1", aggregate, entity, ttl)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      // Should come back in query results
      taskActor ! ScheduleTask("test2", aggregate, entity, ttl)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      // Should NOT come back in query results
      taskActor ! ScheduleTask("test3", aggregate, "1", ttl)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      // Query
      viewActor ! GetTasks(QueryType.ENTITY, entity = Some(entity))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.size must be(2)
          result.tasks.foreach(_.entity must be(entity))
      }
    }
    "Successfully clear state on a TaskExpiration event" in {
      // Wait for tasks to expire
      Thread.sleep(2.seconds.toMillis)
      // Query view state
      viewActor ! GetTasks(QueryType.ENTITY, entity = Some(entity))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.isEmpty must be(true)
      }
    }
  }
}
