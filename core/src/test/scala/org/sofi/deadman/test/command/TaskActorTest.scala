package org.sofi.deadman.test.command

import org.sofi.deadman.messages.command._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class TaskActorTest extends TestSystem {
  "A task actor" must {
    "Successfully schedule a task" in {
      taskActor ! ScheduleTask("test", aggregate, "0", 10.days.toMillis)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
    }
    "Successfully complete a task" in {
      taskActor ! CompleteTask("test", aggregate, "0")
      expectMsg(CommandResponse(ResponseType.SUCCESS))
    }
  }
}
