package org.sofi.deadman.http.request

// Represents an unvalidated request to complete a task
final case class CompleteRequest(
  key: String,
  aggregate: String,
  entity: String
)
