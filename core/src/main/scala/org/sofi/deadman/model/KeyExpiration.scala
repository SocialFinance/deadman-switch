package org.sofi.deadman.model

final case class KeyExpiration(
  key: String,
  window: String,
  expiration: Long,
  aggregate: String,
  entity: String,
  ttl: Long,
  creation: Long,
  tags: Set[String]
)

object KeyExpiration {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.messages.event.Task
  import org.sofi.deadman.storage._, db._

  implicit class KeyExpirationOps(val e: KeyExpiration) extends AnyVal {
    def asTask: Task = Task(e.key, e.aggregate, e.entity, e.creation, e.ttl, Seq.empty, e.tags.toSeq.sorted)
  }

  // Get expirations for the given key and time window, limited to a set time range
  def select(key: String, window: String, start: Long, end: Long)(implicit ec: ExecutionContext): Future[Seq[KeyExpiration]] =
    db.run {
      quote {
        query[KeyExpiration]
          .filter(_.key == lift(key))
          .filter(_.window == lift(window))
          .filter(_.expiration >= lift(start))
          .filter(_.expiration <= lift(end))
      }
    }

  // Create a key expiration record in C*
  def save(models: List[KeyExpiration])(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        liftQuery(models).foreach { model ⇒
          query[KeyExpiration].insert(model).ifNotExists
        }
      }
    }
}
