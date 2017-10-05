package org.sofi.deadman.component.counter

import akka.actor._
import org.sofi.deadman.messages.query._

final class CounterActor(val key: String) extends Actor with ActorLogging {
  private var count: Long = 0
  def receive = {
    case CounterActor.Increment ⇒
      log.debug(s"Increment: $key")
      count = count + 1
    case CounterActor.Decrement ⇒
      log.debug(s"Decrement: $key")
      count = count - 1
    case _: GetCount ⇒
      log.debug(s"GetCount: $key")
      sender() ! Count(count)
  }
}

object CounterActor {
  case object Increment
  case object Decrement
  def props(key: String): Props = Props(new CounterActor(key))
}
