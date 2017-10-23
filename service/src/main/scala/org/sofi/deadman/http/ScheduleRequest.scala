package org.sofi.deadman.http

// Represents an unvalidated request to schedule a task
private[http] final case class ScheduleRequest(
  key: String,
  aggregate: String,
  entity: String,
  ttl: Long,
  ttw: Seq[Long] = Seq.empty,
  tags: Seq[String] = Seq.empty,
  ts: scala.Option[Long] = None
)
