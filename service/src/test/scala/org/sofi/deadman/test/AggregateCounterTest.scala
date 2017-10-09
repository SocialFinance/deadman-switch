package org.sofi.deadman.test

import akka.actor.ActorRef
import com.rbmhtechnology.eventuate.EventsourcedView
import org.sofi.deadman.component.counter._
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.query._
import scala.concurrent.duration._

final class AggregateCounterTest extends TestSystem {

  // Counter
  val counterActor = system.actorOf(AggregateCounter.props(AggregateCounter.name(aggregate), eventLog))

  // Helper view that forwards a `Count` query response back to the test actor for assertion
  final class CountForwarder(val id: String, val eventLog: ActorRef) extends EventsourcedView {
    def onCommand = {
      case _ ⇒
    }
    def onEvent = {
      case event: Count ⇒
        testActor ! event
    }
  }

  "An aggregate view" must {
    "Successfully count Task events" in {
      taskActor ! ScheduleTask("test", aggregate, "0", 10.days.toMillis)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      taskActor ! ScheduleTask("test", aggregate, "1", 10.days.toMillis)
      expectMsg(CommandResponse("", CommandResponse.ResponseType.SUCCESS))
      counterActor ! GetCount(QueryType.AGGREGATE, aggregate = Some(aggregate))
      expectMsgPF() {
        case Count(count) ⇒
          count must be(2)
      }
    }
  }
}
