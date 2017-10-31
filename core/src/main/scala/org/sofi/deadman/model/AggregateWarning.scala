package org.sofi.deadman.model

final case class AggregateWarning(
  aggregate: String,
  key: String,
  ttw: Long,
  entity: String,
  ttl: Long,
  creation: Long,
  warning: Long,
  tags: String
)

object AggregateWarning {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.messages.event.Task
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on warning model
  implicit class AggregateWarningOps(val w: AggregateWarning) extends AnyVal {
    def asTask: Task = Task(w.key, w.aggregate, w.entity, w.creation, w.ttl, Seq(w.ttw), w.tags.split(","))
  }

  // Get warnings for an aggregate
  def select(aggregate: String)(implicit ec: ExecutionContext): Future[Seq[AggregateWarning]] =
    db.run {
      quote {
        query[AggregateWarning].filter(_.aggregate == lift(aggregate))
      }
    }

  // Create a aggregate warning record in C*
  def save(models: List[AggregateWarning])(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        liftQuery(models).foreach { w ⇒
          query[AggregateWarning]
            .filter(_.aggregate == w.aggregate)
            .filter(_.entity == w.entity)
            .filter(_.key == w.key)
            .filter(_.ttw == w.ttw)
            .update(
              _.ttl -> w.ttl,
              _.creation -> w.creation,
              _.warning -> w.warning,
              _.tags -> w.tags
            )
        }
      }
    }
}
