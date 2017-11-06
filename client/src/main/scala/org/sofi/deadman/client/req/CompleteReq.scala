package org.sofi.deadman.client.req

final case class CompleteReq(
  key: String,
  aggregate: String,
  entity: String
)
