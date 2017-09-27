package org.sofi.deadman.model

final case class TaggedViolation(
  tag: String,
  window: String,
  expiration: Long,
  aggregate: String,
  entity: String,
  key: String,
  ttl: Long,
  creation: Long,
  tags: String
)

object TaggedViolation {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on a tagged violation model
  implicit class TaggedViolationOps(val t: TaggedViolation) extends AnyVal {
    def save(implicit ec: ExecutionContext): Future[Unit] = TaggedViolation.save(t)
  }

  // Get violations for the given tag and time window, limited to a set time range
  def select(tag: String, window: String, start: Long, end: Long)(implicit ec: ExecutionContext): Future[Seq[TaggedViolation]] =
    db.run {
      quote {
        query[TaggedViolation]
          .filter(_.tag == lift(tag))
          .filter(_.window == lift(window))
          .filter(_.expiration >= lift(start))
          .filter(_.expiration <= lift(end))
      }
    }

  // Create a tagged violation record in C*
  def save(t: TaggedViolation)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[TaggedViolation]
          .filter(_.tag == lift(t.tag))
          .filter(_.window == lift(t.window))
          .filter(_.expiration == lift(t.expiration))
          .update(
            _.aggregate -> lift(t.aggregate),
            _.entity -> lift(t.entity),
            _.key -> lift(t.key),
            _.ttl -> lift(t.ttl),
            _.creation -> lift(t.creation),
            _.tags -> lift(t.tags)
          )
      }
    }
}
