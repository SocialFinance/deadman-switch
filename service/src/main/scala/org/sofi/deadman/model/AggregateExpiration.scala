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
  import org.sofi.deadman.messages.event.Task
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on expiration model
  implicit class AggregateExpirationOps(val e: AggregateExpiration) extends AnyVal {
    def asTask: Task = Task(e.key, e.aggregate, e.entity, e.creation, e.ttl, Seq.empty, e.tags.split(","))
    def save(implicit ec: ExecutionContext): Future[Unit] = AggregateExpiration.save(e)
  }

  // Get expirations for an aggregate
  def select(aggregate: String)(implicit ec: ExecutionContext): Future[Seq[AggregateExpiration]] =
    db.run {
      quote {
        query[AggregateExpiration].filter(_.aggregate == lift(aggregate))
      }
    }

  // Create a expiration record in C*
  def save(e: AggregateExpiration)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[AggregateExpiration]
          .filter(_.aggregate == lift(e.aggregate))
          .filter(_.entity == lift(e.entity))
          .filter(_.key == lift(e.key))
          .update(
            _.ttl -> lift(e.ttl),
            _.creation -> lift(e.creation),
            _.expiration -> lift(e.expiration),
            _.tags -> lift(e.tags)
          )
      }
    }
}
