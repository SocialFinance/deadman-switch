package org.sofi.deadman.location

import com.rbmhtechnology.eventuate._
import org.sofi.deadman.log.EventLog
import org.sofi.deadman.messages.event._

// A replication filter that selectively filters warning and expiration events
private[location] final class Filter(passive: Boolean) extends ReplicationFilter {

  // When in 'passive' mode, do NOT replicate warning and expiration events
  def apply(event: DurableEvent) = event.payload match {
    case _: Task ⇒ true
    case _: TaskTermination ⇒ true
    case _: TaskWarning ⇒ !passive
    case _: TaskExpiration ⇒ !passive
    case _ ⇒ false
  }
}

// Factory companion object
private[location] object Filter {
  def apply(passive: Boolean) = EndpointFilters.sourceFilters(Map(EventLog.name -> new Filter(passive)))
}
