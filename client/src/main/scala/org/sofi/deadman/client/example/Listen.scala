package org.sofi.deadman.client
package example

import org.sofi.deadman.client.stream._
import org.sofi.deadman.messages.event._
import scala.util.Try

// Example deadman switch client `event stream` usage
object Listen extends App {

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
    override val offset = Try(args(0).toLong).toOption
    override val aggregate = Some("1")
    override val filter = expirations or warnings
  }

  // Print task warnings and expirations to stdout
  val stream = EventStream(settings)
  stream.events
    .map(_.payload)
    .runForeach { e ⇒
      println(s"${e.getClass.getName}")
      println(s"${e.toString}")
    }
}
