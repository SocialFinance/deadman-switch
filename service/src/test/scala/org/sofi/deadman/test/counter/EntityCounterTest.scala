package org.sofi.deadman.test.counter

import org.sofi.deadman.component.counter._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class EntityCounterTest extends TestSystem {

  // Entity counter actor
  private val counterActor = system.actorOf(EntityCounter.props(EntityCounter.name(aggregate), eventLog))

  "An entity counter" must {
    "Successfully count Task events" in {
      taskActor ! ScheduleTask("test1", aggregate, "0", 10.days.toMillis)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      taskActor ! ScheduleTask("test2", aggregate, "0", 10.days.toMillis)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      counterActor ! GetCount(QueryType.ENTITY, entity = Some("0"))
      expectMsgPF() {
        case Count(count) ⇒
          count must be(2)
      }
    }
  }
}
