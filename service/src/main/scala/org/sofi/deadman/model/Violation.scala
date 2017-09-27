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
  implicit class ViolationOps(val v: Violation) extends AnyVal {
    def save(implicit ec: ExecutionContext): Future[Unit] = Violation.save(v)
  }

  // Get violations for an aggregate
  def select(aggregate: String)(implicit ec: ExecutionContext): Future[Seq[Violation]] =
    db.run {
      quote {
        query[Violation].filter(_.aggregate == lift(aggregate))
      }
    }

  // Create a violation record in C*
  def save(v: Violation)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[Violation]
          .filter(_.aggregate == lift(v.aggregate))
          .filter(_.entity == lift(v.entity))
          .filter(_.key == lift(v.key))
          .update(
            _.ttl -> lift(v.ttl),
            _.creation -> lift(v.creation),
            _.expiration -> lift(v.expiration),
            _.tags -> lift(v.tags)
          )
      }
    }
}
