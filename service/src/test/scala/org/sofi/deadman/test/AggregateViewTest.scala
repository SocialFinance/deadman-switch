package org.sofi.deadman.test

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import com.typesafe.config.ConfigFactory
import java.util.UUID
import org.scalatest.{ MustMatchers, WordSpecLike }
import org.sofi.deadman.component.actor.TaskActor
import org.sofi.deadman.component.view.ViewManager
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._

final class AggregateViewTest extends TestKit(ActorSystem("test-actor-system", ConfigFactory.load("test")))
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

  "An aggregate view" must {
    "Successfully receive a Task event" in {
      taskActor ! ScheduleTask("test", agg, "0", 100L)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      viewManager ! GetTasks(GetTasks.ViewType.AGGREGATE, aggregate = Some(agg))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.size must be(1)
          result.tasks.foreach(_.aggregate must be(agg))
      }
    }
    "Successfully clear state on a TaskExpiration event" in {
      // Wait for task to expire
      Thread.sleep(1100L)
      // Query view state
      viewManager ! GetTasks(GetTasks.ViewType.AGGREGATE, aggregate = Some(agg))
      expectMsgPF() {
        case result: Tasks ⇒
          result.tasks.isEmpty must be(true)
      }
    }
  }
}
