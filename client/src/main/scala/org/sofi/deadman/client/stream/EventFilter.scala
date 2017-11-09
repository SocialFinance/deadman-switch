package org.sofi.deadman.client.stream

// Application level filter
trait EventFilter { self ⇒

  // Determine whether some event should be filtered
  def apply(event: Any): Boolean = true

  // Combine this filter with another, requiring that both results evaluate to true
  def and(other: EventFilter) = new EventFilter {
    override def apply(event: Any): Boolean = self(event) && other(event)
  }

  // Combine this filter with another, requiring that only one result evaluates to true
  def or(other: EventFilter) = new EventFilter {
    override def apply(event: Any): Boolean = if (self(event)) true else other(event)
  }
}
