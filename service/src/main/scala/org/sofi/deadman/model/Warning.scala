package org.sofi.deadman.model

final case class Warning(
  aggregate: String,
  entity: String,
  key: String,
  ttw: Long,
  creation: Long,
  warning: Long,
  tags: String
)

object Warning {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on warning model
  implicit class ComplianceWarningOps(val w: Warning) extends AnyVal {
    def save(implicit ec: ExecutionContext): Future[Unit] = Warning.save(w)
  }

  // Get warnings for an aggregate
  def select(aggregate: String)(implicit ec: ExecutionContext): Future[Seq[Warning]] =
    db.run {
      quote {
        query[Warning].filter(_.aggregate == lift(aggregate))
      }
    }

  // Create a warning record in C*
  def save(w: Warning)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[Warning]
          .filter(_.aggregate == lift(w.aggregate))
          .filter(_.entity == lift(w.entity))
          .filter(_.key == lift(w.key))
          .filter(_.ttw == lift(w.ttw))
          .update(
            _.creation -> lift(w.creation),
            _.warning -> lift(w.warning),
            _.tags -> lift(w.tags)
          )
      }
    }
}
