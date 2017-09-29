package org.sofi.deadman.test

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import com.typesafe.config.ConfigFactory
import org.scalatest.{ MustMatchers, WordSpecLike }
import org.sofi.deadman.component.actor.TaskActor
import org.sofi.deadman.component.view.ViewManager
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._

final class EntityViewTest extends TestKit(ActorSystem("test-actor-system", ConfigFactory.load("test")))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with SystemTermination {

  // Random aggregate ID
  val agg = UUID.randomUUID().toString

  // LevelDB event log (local file-system)
  val eventLog = system.actorOf(LeveldbEventLog.props(UUID.randomUUID().toString))

  // Command actor
  val taskActor = system.actorOf(TaskActor.props(agg, "test", eventLog))

  // View
  val viewManager = system.actorOf(ViewManager.props(agg, eventLog))

  "An entity view" must {
    "Successfully receive Task events" in {
      // Should come back in query results
      taskActor ! ScheduleTask("test1", agg, "0", 100L)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      // Should come back in query results
      taskActor ! ScheduleTask("test2", agg, "0", 100L)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      // Should NOT come back in query results
      taskActor ! ScheduleTask("test3", agg, "1", 100L)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      // Query
      viewManager ! GetTasks(GetTasks.ViewType.ENTITY, entity = Some("0"))
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
      viewManager ! GetTasks(GetTasks.ViewType.ENTITY, entity = Some("0"))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.isEmpty must be(true)
      }
    }
  }
}
