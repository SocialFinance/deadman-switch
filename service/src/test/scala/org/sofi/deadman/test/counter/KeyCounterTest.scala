package org.sofi.deadman.test.counter

import org.sofi.deadman.component.counter._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class KeyCounterTest extends TestSystem {

  // Key counter actor
  val counterActor = system.actorOf(KeyCounter.props(KeyCounter.name(aggregate), eventLog))

  "A key counter" must {
    "Successfully count Task events" in {
      taskActor ! ScheduleTask("test", aggregate, "0", 10.days.toMillis)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      taskActor ! ScheduleTask("test", aggregate, "0", 10.days.toMillis)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      counterActor ! GetCount(QueryType.KEY, key = Some("test"))
      expectMsgPF() {
        case Count(count) ⇒
          count must be(2)
      }
    }
  }
}
