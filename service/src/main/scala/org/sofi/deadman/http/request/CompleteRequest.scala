package org.sofi.deadman.http.request

// Represents an unvalidated request to complete a task
final case class CompleteRequest(
  key: String,
  aggregate: String,
  entity: String
)

object CompleteRequest {
  import org.sofi.deadman.messages._
  implicit class CompleteRequestOps(val r: CompleteRequest) extends AnyVal {
    def validate = validation.validateCompletion(r.key, r.aggregate, r.entity)
  }
}
