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
  implicit class ComplianceWarningOps(val c: Warning) extends AnyVal {
    def save(implicit ec: ExecutionContext): Future[Unit] = Warning.save(c)
  }

  // Create a warning record in C*
  def save(c: Warning)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[Warning]
          .filter(_.aggregate == lift(c.aggregate))
          .filter(_.entity == lift(c.entity))
          .filter(_.key == lift(c.key))
          .filter(_.ttw == lift(c.ttw))
          .update(
            _.creation -> lift(c.creation),
            _.warning -> lift(c.warning),
            _.tags -> lift(c.tags)
          )
      }
    }
}
