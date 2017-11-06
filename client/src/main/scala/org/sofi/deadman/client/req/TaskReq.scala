package org.sofi.deadman.client.req

final case class TaskReq(
  aggregate: String,
  entity: String,
  key: String,
  ttl: String,
  ts: Long = System.currentTimeMillis(),
  ttw: Seq[String] = Seq.empty,
  tags: Seq[String] = Seq.empty
)
