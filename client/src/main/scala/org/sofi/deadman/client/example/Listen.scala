package org.sofi.deadman.client.example

import org.sofi.deadman.client._
import org.sofi.deadman.client.stream._
import org.sofi.deadman.messages.event._
import scala.util.Try

// Example deadman switch client `event stream` usage
object Listen extends App {

  // Filter out everything except expirations
  final val expirations = new Filter {
    override def apply(any: Any) = any.isInstanceOf[TaskExpiration]
  }

  // Filter out everything except warnings
  final val warnings = new Filter {
    override def apply(any: Any) = any.isInstanceOf[TaskWarning]
  }

  // Only show warnings and expirations for aggregate "1" starting with a given sequence number offset
  final val settings = new Settings {
    val id = "deadman-event-stream-example"
    override val offset = Try(args(0).toLong).toOption
    override val aggregate = Some("1")
    override val filter = expirations or warnings
  }

  // Print task warnings and expirations to stdout
  EventStream(settings).events
    .map(_.payload)
    .runForeach { e ⇒
      println(s"${e.getClass.getName}")
      println(s"${e.toString}")
    }
}
