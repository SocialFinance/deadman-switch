package org.sofi.deadman.component.processor

import com.rbmhtechnology.eventuate.EventsourcedProcessor
import java.text.SimpleDateFormat
import java.util.{ Date, TimeZone }

private[processor] trait EventProcessor extends EventsourcedProcessor {

  // Time window format function
  private def dateString(pattern: String): String = {
    val fmt = new SimpleDateFormat(pattern)
    fmt.setTimeZone(TimeZone.getTimeZone("US/Eastern"))
    fmt.format(new Date())
  }

  // Get the current day, week of year and month time windows
  protected def windows: Seq[String] = Seq(dateString("yyyy-MM-dd"), dateString("yyyy-'week'-ww"), dateString("yyyy-MM"))

  // Ignore commands by default
  def onCommand: Receive = {
    case _ ⇒
  }
}
