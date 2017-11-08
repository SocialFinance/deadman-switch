package org.sofi.deadman.client.stream

// Application level filter
trait EventFilter { self ⇒

  // Determine whether some event should be filtered
  def apply(any: Any): Boolean = true

  // Combine this filter with another, requiring that both results evaluate to true
  def and(other: EventFilter) = new EventFilter {
    override def apply(any: Any): Boolean = self(any) && other(any)
  }

  // Combine this filter with another, requiring that only one result evaluates to true
  def or(other: EventFilter) = new EventFilter {
    override def apply(any: Any): Boolean = if (self(any)) true else other(any)
  }
}
