package org.sofi.deadman.load

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.sofi.deadman.client.stream._
import org.sofi.deadman.messages.event._

object Listener extends App {

  // Create an actor system
  implicit val actorSystem = ActorSystem("deadman-switch-actor-system", ConfigFactory.load("stream").resolve())
  implicit val materializer = ActorMaterializer()

  // Filter out everything except expirations
  val expirations = new EventFilter {
    override def apply(any: Any) = any.isInstanceOf[TaskExpiration]
  }

  // Filter out everything except warnings
  val warnings = new EventFilter {
    override def apply(any: Any) = any.isInstanceOf[TaskWarning]
  }

  // Only show warnings and expirations for aggregate "1" starting with a given sequence number offset
  val settings = new StreamSettings {
    val id = "deadman-event-stream-example"
    override val aggregate = Some("1")
    override val filter = expirations or warnings
  }

  // Print task warnings and expirations to stdout
  EventStream(settings).events.runForeach { event ⇒
    val payload = event.payload
    println(s"${event.localSequenceNr} ${payload.getClass.getName}\n${payload.toString}")
  }
}
