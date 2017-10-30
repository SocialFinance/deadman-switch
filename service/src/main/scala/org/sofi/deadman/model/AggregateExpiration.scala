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
  }

  // Get expirations for an aggregate
  def select(aggregate: String)(implicit ec: ExecutionContext): Future[Seq[AggregateExpiration]] =
    db.run {
      quote {
        query[AggregateExpiration].filter(_.aggregate == lift(aggregate))
      }
    }

  // Create a series of expiration records in C*
  def save(models: List[AggregateExpiration])(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        liftQuery(models).foreach { e ⇒
          query[AggregateExpiration]
            .filter(_.aggregate == e.aggregate)
            .filter(_.entity == e.entity)
            .filter(_.key == e.key)
            .update(
              _.ttl -> e.ttl,
              _.creation -> e.creation,
              _.expiration -> e.expiration,
              _.tags -> e.tags
            )
        }
      }
    }
}
