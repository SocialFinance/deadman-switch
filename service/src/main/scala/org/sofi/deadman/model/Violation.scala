package org.sofi.deadman.model

final case class Violation(
  aggregate: String,
  entity: String,
  key: String,
  ttl: Long,
  creation: Long,
  expiration: Long,
  tags: String
)

object Violation {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on violation model
  implicit class ViolationOps(val c: Violation) extends AnyVal {
    def save(implicit ec: ExecutionContext): Future[Unit] = Violation.save(c)
  }

  // Create a violation record in C*
  def save(c: Violation)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[Violation]
          .filter(_.aggregate == lift(c.aggregate))
          .filter(_.entity == lift(c.entity))
          .filter(_.key == lift(c.key))
          .update(
            _.ttl -> lift(c.ttl),
            _.creation -> lift(c.creation),
            _.expiration -> lift(c.expiration),
            _.tags -> lift(c.tags)
          )
      }
    }
}
