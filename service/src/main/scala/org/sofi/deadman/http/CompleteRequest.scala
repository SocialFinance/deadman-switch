package org.sofi.deadman.http

// Represents an unvalidated request to complete a task
private[http] final case class CompleteRequest(
  key: String,
  aggregate: String,
  entity: String
)
