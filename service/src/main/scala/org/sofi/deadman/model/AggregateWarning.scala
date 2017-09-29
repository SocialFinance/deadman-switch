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
  implicit class ComplianceWarningOps(val w: AggregateWarning) extends AnyVal {
    def asTask: Task = Task(w.key, w.aggregate, w.entity, w.creation, w.creation, Seq(w.ttw), w.tags.split(","))
    def save(implicit ec: ExecutionContext): Future[Unit] = AggregateWarning.save(w)
  }

  // Get warnings for an aggregate
  def select(aggregate: String)(implicit ec: ExecutionContext): Future[Seq[AggregateWarning]] =
    db.run {
      quote {
        query[AggregateWarning].filter(_.aggregate == lift(aggregate))
      }
    }

  // Create a aggregate warning record in C*
  def save(w: AggregateWarning)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[AggregateWarning]
          .filter(_.aggregate == lift(w.aggregate))
          .filter(_.key == lift(w.key))
          .filter(_.ttw == lift(w.ttw))
          .update(
            _.entity -> lift(w.entity),
            _.ttl -> lift(w.ttl),
            _.creation -> lift(w.creation),
            _.warning -> lift(w.warning),
            _.tags -> lift(w.tags)
          )
      }
    }
}
