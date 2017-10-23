package org.sofi.deadman.http

private[http] final case class CompleteRequest(
  key: String,
  aggregate: String,
  entity: String
)
