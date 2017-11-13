package org.sofi.deadman.load

import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.sofi.deadman.client.stream._

// Listen for task expiration and warning events emitted from the deadman switch service
object Listen extends App {

  // Create an actor system
  implicit val actorSystem = EventStream.actorSystem(ConfigFactory.load("stream").resolve())
  implicit val materializer = ActorMaterializer()

  // Show all events for aggregate "1"
  val settings = new StreamSettings {
    val id = "deadman-event-stream-example"
    override val aggregate = Some("1")
  }

  // Print task warnings and expirations to stdout
  EventStream(settings).events
    .filter(_.processId == "loc1_L1") // Hack to filter out dup warnings and expirations
    .runForeach { event ⇒
      val payload = event.payload
      println(s"${event.localSequenceNr} ${payload.getClass.getName}\n${payload.toString}")
    }
}
