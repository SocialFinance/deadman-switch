package org.sofi.deadman.component.counter

import akka.actor._
import org.sofi.deadman.messages.query._

final class CounterManager(val id: String, val eventLog: ActorRef) extends Actor {

  // Aggregate counter reference
  private val aggCounter = context.actorOf(AggregateCounter.props(AggregateCounter.name(id), eventLog))

  // Entity counter reference
  private val entCounter = context.actorOf(EntityCounter.props(EntityCounter.name(id), eventLog))

  // Key counter reference
  private val keyCounter = context.actorOf(KeyCounter.props(KeyCounter.name(id), eventLog))

  // Forward counter query to the correct actor
  override def receive = {
    case query: GetCount ⇒
      query.queryType match {
        case QueryType.ENTITY ⇒ entCounter forward query
        case QueryType.KEY ⇒ keyCounter forward query
        case _ ⇒ aggCounter forward query
      }
  }
}

object CounterManager {
  def name(id: String): String = s"$id-counter-manager"
  def props(id: String, eventLog: ActorRef): Props = Props(new CounterManager(id, eventLog))
}
