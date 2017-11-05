package org.sofi.deadman.http.request

// Represents an unvalidated request to schedule a task
final case class ScheduleRequest(
  key: String,
  aggregate: String,
  entity: String,
  ttl: String,
  ttw: Seq[String] = Seq.empty,
  tags: Seq[String] = Seq.empty,
  ts: Option[Long] = None
)

object ScheduleRequest {
  import org.sofi.deadman.messages._
  implicit class ScheduleRequestOps(val r: ScheduleRequest) extends AnyVal {
    def ensureTimestamp = if (r.ts.isDefined) r else r.copy(ts = Some(System.currentTimeMillis()))
    def validate = validation.validate(r.key, r.aggregate, r.entity, r.ttl, r.ttw, r.tags, r.ts)
  }
}
