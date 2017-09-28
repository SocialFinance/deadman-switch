package org.sofi.deadman.model

final case class AggregateExpiration(
  aggregate: String,
  entity: String,
  key: String,
  ttl: Long,
  creation: Long,
  expiration: Long,
  tags: String
)

object AggregateExpiration {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on violation model
  implicit class ViolationOps(val v: AggregateExpiration) extends AnyVal {
    def save(implicit ec: ExecutionContext): Future[Unit] = AggregateExpiration.save(v)
  }

  // Get violations for an aggregate
  def select(aggregate: String)(implicit ec: ExecutionContext): Future[Seq[AggregateExpiration]] =
    db.run {
      quote {
        query[AggregateExpiration].filter(_.aggregate == lift(aggregate))
      }
    }

  // Create a violation record in C*
  def save(e: AggregateExpiration)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[AggregateExpiration]
          .filter(_.aggregate == lift(e.aggregate))
          .filter(_.key == lift(e.key))
          .update(
            _.entity -> lift(e.entity),
            _.ttl -> lift(e.ttl),
            _.creation -> lift(e.creation),
            _.expiration -> lift(e.expiration),
            _.tags -> lift(e.tags)
          )
      }
    }
}
