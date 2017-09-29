package org.sofi.deadman.test

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import com.typesafe.config.ConfigFactory
import org.scalatest.{ MustMatchers, WordSpecLike }
import org.sofi.deadman.component.actor.TaskActor
import org.sofi.deadman.messages.command._

final class TaskActorTest extends TestKit(ActorSystem("test-actor-system", ConfigFactory.load("test")))
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

  "A task actor" must {
    "Successfully schedule a task" in {
      taskActor ! ScheduleTask("test", agg, "0", 1000000L)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
    }
    "Successfully complete a task" in {
      taskActor ! CompleteTask("test", agg, "0")
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
    }
  }
}
