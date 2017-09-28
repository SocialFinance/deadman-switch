package org.sofi.deadman.component.processor

import akka.actor._
import org.sofi.deadman.messages.event._
import java.util.{ Date, TimeZone }
import java.text.SimpleDateFormat

final class TaskExpirationProcessor(val id: String, val eventLog: ActorRef, val targetEventLog: ActorRef) extends EventProcessor {

  // Time window format function
  private def format(pattern: String): String = {
    val fmt = new SimpleDateFormat(pattern)
    fmt.setTimeZone(TimeZone.getTimeZone("US/Eastern"))
    fmt.format(new Date())
  }

  // Get the current day, week of year and month time windows
  private def windows: Seq[String] = Seq(format("yyyy-MM-dd"), format("yyyy-ww"), format("yyyy-MM"))

  // Create a tagged expiration set when a task expires
  def processEvent = {
    case TaskExpiration(task, ts) ⇒
      task.tags.flatMap(tag ⇒ windows.map(window ⇒ TaggedExpiration(task, tag, window, ts))).toIndexedSeq
  }
}

object TaskExpirationProcessor {
  def name(id: String): String = s"$id-task-expiration-processor"
  def props(id: String, eventLog: ActorRef, targetEventLog: ActorRef): Props = Props(
    new TaskExpirationProcessor(id, eventLog, targetEventLog)
  )
}
