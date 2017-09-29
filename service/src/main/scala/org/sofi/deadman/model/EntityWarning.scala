package org.sofi.deadman.model

final case class EntityWarning(
  entity: String,
  key: String,
  ttw: Long,
  aggregate: String,
  ttl: Long,
  creation: Long,
  warning: Long,
  tags: String
)

object EntityWarning {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.messages.event.Task
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on entity warning model
  implicit class EntityWarningOps(val w: EntityWarning) extends AnyVal {
    def asTask: Task = Task(w.key, w.aggregate, w.entity, w.creation, w.ttl, Seq(w.ttw), w.tags.split(","))
    def save(implicit ec: ExecutionContext): Future[Unit] = EntityWarning.save(w)
  }

  // Get warnings for an aggregate
  def select(entity: String)(implicit ec: ExecutionContext): Future[Seq[EntityWarning]] =
    db.run {
      quote {
        query[EntityWarning].filter(_.entity == lift(entity))
      }
    }

  // Create a aggregate warning record in C*
  def save(w: EntityWarning)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[EntityWarning]
          .filter(_.entity == lift(w.entity))
          .filter(_.key == lift(w.key))
          .filter(_.ttw == lift(w.ttw))
          .update(
            _.aggregate -> lift(w.aggregate),
            _.ttl -> lift(w.ttl),
            _.creation -> lift(w.creation),
            _.warning -> lift(w.warning),
            _.tags -> lift(w.tags)
          )
      }
    }
}
