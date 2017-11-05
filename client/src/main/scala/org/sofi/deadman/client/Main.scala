package org.sofi.deadman.client

import org.sofi.deadman.messages.event._
import scala.util.Try

// Example deadman switch client usage
object Main extends App {

  // Put implicits in scope
  import EventStream._

  // Filter out everything except task warnings and expirations
  final val eventFilter = new Filter {
    override def apply(any: Any) = any match {
      case _: TaskExpiration ⇒ true
      case _: TaskWarning ⇒ true
      case _ ⇒ false
    }
  }

  // Set offset, aggregate and filter
  final val settings = new Settings {
    val id = "deadman-client-source-test"
    override val offset = Try(args(0).toLong).toOption
    override val aggregate = Some("1")
    override def filter = eventFilter
  }

  // Print task warnings and expirations to stdout
  EventStream(settings).events
    .map(_.payload)
    .runForeach(e ⇒ println(s"${e.getClass.getName}\n${e.toString}"))
}
