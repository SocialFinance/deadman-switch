package org.sofi.deadman.model

final case class KeyExpiration(
  key: String,
  window: String,
  expiration: Long,
  aggregate: String,
  entity: String,
  ttl: Long,
  creation: Long,
  tags: String
)

object KeyExpiration {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.messages.event.Task
  import org.sofi.deadman.storage._, db._

  implicit class TagExpirationOps(val e: KeyExpiration) extends AnyVal {
    def asTask: Task = Task(e.key, e.aggregate, e.entity, e.creation, e.ttl, Seq.empty, e.tags.split(","))
    def save(implicit ec: ExecutionContext): Future[Unit] = KeyExpiration.save(e)
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
  def save(e: KeyExpiration)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[KeyExpiration]
          .filter(_.key == lift(e.key))
          .filter(_.window == lift(e.window))
          .filter(_.expiration == lift(e.expiration))
          .filter(_.aggregate == lift(e.aggregate))
          .filter(_.entity == lift(e.entity))
          .update(
            _.ttl -> lift(e.ttl),
            _.creation -> lift(e.creation),
            _.tags -> lift(e.tags)
          )
      }
    }
}
