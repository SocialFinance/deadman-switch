package org.sofi.deadman.client.req

final case class CompleteReq(
  aggregate: String,
  entity: String,
  key: String
)
