package org.sofi.deadman.model

final case class EntityExpiration(
  entity: String,
  key: String,
  aggregate: String,
  ttl: Long,
  creation: Long,
  expiration: Long,
  tags: String
)

object EntityExpiration {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on entity warning model
  implicit class EntityExpirationOps(val w: EntityExpiration) extends AnyVal {
    def save(implicit ec: ExecutionContext): Future[Unit] = EntityExpiration.save(w)
  }

  // Get warnings for an aggregate
  def select(entity: String)(implicit ec: ExecutionContext): Future[Seq[EntityExpiration]] =
    db.run {
      quote {
        query[EntityExpiration].filter(_.entity == lift(entity))
      }
    }

  // Create a aggregate warning record in C*
  def save(w: EntityExpiration)(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        query[EntityExpiration]
          .filter(_.entity == lift(w.entity))
          .filter(_.key == lift(w.key))
          .update(
            _.aggregate -> lift(w.aggregate),
            _.ttl -> lift(w.ttl),
            _.creation -> lift(w.creation),
            _.expiration -> lift(w.expiration),
            _.tags -> lift(w.tags)
          )
      }
    }
}
