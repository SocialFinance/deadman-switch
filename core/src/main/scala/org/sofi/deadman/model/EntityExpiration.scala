package org.sofi.deadman.model

final case class EntityExpiration(
  entity: String,
  key: String,
  aggregate: String,
  ttl: Long,
  creation: Long,
  expiration: Long,
  tags: Set[String]
)

object EntityExpiration {
  import scala.concurrent.{ ExecutionContext, Future }
  import org.sofi.deadman.messages.event.Task
  import org.sofi.deadman.storage._, db._

  // Syntactic sugar on entity expiration model
  implicit class EntityExpirationOps(val e: EntityExpiration) extends AnyVal {
    def asTask: Task = Task(e.key, e.aggregate, e.entity, e.creation, e.ttl, Seq.empty, e.tags.toSeq.sorted)
  }

  // Get expirations for an entity
  def select(entity: String)(implicit ec: ExecutionContext): Future[Seq[EntityExpiration]] =
    db.run {
      quote {
        query[EntityExpiration].filter(_.entity == lift(entity))
      }
    }

  // Create a entity expiration record in C*
  def save(models: List[EntityExpiration])(implicit ec: ExecutionContext): Future[Unit] =
    db.run {
      quote {
        liftQuery(models).foreach { model ⇒
          query[EntityExpiration].insert(model).ifNotExists
        }
      }
    }
}
