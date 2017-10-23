package org.sofi.deadman.test.counter

import org.sofi.deadman.component.counter._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import org.sofi.deadman.test.TestSystem
import scala.concurrent.duration._

final class AggregateCounterTest extends TestSystem {

  // Counter actor
  private val counterActor = system.actorOf(AggregateCounter.props(AggregateCounter.name(aggregate), eventLog))

  "An aggregate counter" must {
    "Successfully count Task events" in {
      taskActor ! ScheduleTask("test", aggregate, "0", 10.days.toMillis)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      taskActor ! ScheduleTask("test", aggregate, "1", 10.days.toMillis)
      expectMsg(CommandResponse(ResponseType.SUCCESS))
      counterActor ! GetCount(QueryType.AGGREGATE, aggregate = Some(aggregate))
      expectMsgPF() {
        case Count(count) ⇒
          count must be(2)
      }
    }
  }
}
