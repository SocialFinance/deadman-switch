package org.sofi.deadman.model

final case class TagExpiration(
  tag: String,
  window: String,
  expiration: Long,
  aggregate: String,
  entity: String,
  key: String,
  ttl: Long,
  creation: Long,
  tags: Set[String]
)

object TagExpiration {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.messages.event.Task
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on a tagged expiration model
  implicit class TagExpirationOps(val e: TagExpiration) extends AnyVal {
    def asTask: Task = Task(e.key, e.aggregate, e.entity, e.creation, e.ttl, Seq.empty, e.tags.toSeq.sorted)
  }

  // Get expirations for the given tag and time window, limited to a set time range
  def select(tag: String, window: String, start: Long, end: Long)(implicit ec: ExecutionContext): Future[Seq[TagExpiration]] =
    db.run {
      quote {
        query[TagExpiration]
          .filter(_.tag == lift(tag))
          .filter(_.window == lift(window))
          .filter(_.expiration >= lift(start))
          .filter(_.expiration <= lift(end))
      }
    }

  // Create a tagged expiration record in C*
  def save(models: List[TagExpiration])(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        liftQuery(models).foreach { model ⇒
          query[TagExpiration].insert(model).ifNotExists
        }
      }
    }
}
