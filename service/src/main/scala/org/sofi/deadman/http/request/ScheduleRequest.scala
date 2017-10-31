package org.sofi.deadman.http.request

// Represents an unvalidated request to schedule a task
final case class ScheduleRequest(
  key: String,
  aggregate: String,
  entity: String,
  ttl: Long,
  ttw: Seq[Long] = Seq.empty,
  tags: Seq[String] = Seq.empty,
  ts: scala.Option[Long] = None
)
