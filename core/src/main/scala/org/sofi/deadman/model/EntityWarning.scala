package org.sofi.deadman.model

final case class EntityWarning(
  entity: String,
  key: String,
  ttw: Long,
  aggregate: String,
  ttl: Long,
  creation: Long,
  warning: Long,
  tags: Set[String]
)

object EntityWarning {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.messages.event.Task
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on entity warning model
  implicit class EntityWarningOps(val w: EntityWarning) extends AnyVal {
    def asTask: Task = Task(w.key, w.aggregate, w.entity, w.creation, w.ttl, Seq(w.ttw), w.tags.toSeq.sorted)
  }

  // Get warnings for an aggregate
  def select(entity: String)(implicit ec: ExecutionContext): Future[Seq[EntityWarning]] =
    db.run {
      quote {
        query[EntityWarning].filter(_.entity == lift(entity))
      }
    }

  // Create a aggregate warning record in C*
  def save(models: List[EntityWarning])(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        liftQuery(models).foreach { w ⇒
          query[EntityWarning]
            .filter(_.entity == w.entity)
            .filter(_.aggregate == w.aggregate)
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
