package org.sofi.deadman.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import org.sofi.deadman.messages.command._, ResponseType._
import scala.concurrent.ExecutionContext

final class HttpRouter(implicit api: ApiFunctions, ec: ExecutionContext) extends JsonProtocol {
  import api._

  // format: OFF

  private val schedule =
    path("deadman" / "api" / "v1" / "task") {
      entity(asSourceOf[ScheduleTask]) { source ⇒
        val scheduled = source.via(scheduleTaskFlow).runFold(Seq.empty[String]) { (s, r) ⇒ s ++ r.map(_.body) }
        onSuccess(scheduled) { errors ⇒
          val status = if (errors.nonEmpty) BadRequest else Created
          complete(status -> Map("errors" -> errors))
        }
      }
    }

  private val scheduleAsync =
    path("deadman" / "api" / "v1" / "task" / "async") {
      entity(as[ScheduleTask]) { task ⇒
        onSuccess(queueTask(task)) { resp ⇒
          val status = if (resp.responseType == QUEUED) OK else ServiceUnavailable
          complete(status -> resp)
        }
      }
    }

  private val completed =
    path("deadman" / "api" / "v1" / "task" / "complete") {
      entity(as[CompleteTask]) { ct ⇒
        onSuccess(completeTask(ct)) { resp ⇒
          val status = if (resp.responseType == SUCCESS) OK else NotFound
          complete(status -> resp)
        }
      }
    }

  private val aggregate =
    pathPrefix("deadman" / "api" / "v1" / "aggregate" / Segment) { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryAggregate(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val aggExpirations =
    pathPrefix("deadman" / "api" / "v1" / "aggregate" / Segment / "expirations") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryAggregateExpirations(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val aggWarnings =
    pathPrefix("deadman" / "api" / "v1" / "aggregate" / Segment / "warnings") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryAggregateWarnings(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val aggCount =
    pathPrefix("deadman" / "api" / "v1" / "aggregate" / Segment / "count") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryAggregateCount(id)) { count ⇒
            complete(count)
          }
        }
      }
    }

  private val entityRoute =
    pathPrefix("deadman" / "api" / "v1" / "entity" / Segment) { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryEntity(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val entExpirations =
    pathPrefix("deadman" / "api" / "v1" / "entity" / Segment / "expirations") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryEntityExpirations(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val entWarnings =
    pathPrefix("deadman" / "api" / "v1" / "entity" / Segment / "warnings") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryEntityWarnings(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val entCount =
    pathPrefix("deadman" / "api" / "v1" / "entity" / Segment / "count") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryEntityCount(id)) { count ⇒
            complete(count)
          }
        }
      }
    }

  private val key =
    pathPrefix("deadman" / "api" / "v1" / "key" / Segment) { key ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryKey(key)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val keyCount =
    pathPrefix("deadman" / "api" / "v1" / "key" / Segment / "count") { key ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryKeyCount(key)) { count ⇒
            complete(count)
          }
        }
      }
    }

  private val tags =
    pathPrefix("deadman" / "api" / "v1" / "tag" / Segment) { tag ⇒
      path(Segment) { window ⇒
        pathEndOrSingleSlash {
          get {
            onSuccess(queryExpiredTag(tag, window)) { tasks ⇒
              complete(tasks)
            }
          }
        }
      }
    }

  // Combine all endpoints
  val routes =
    schedule ~
    scheduleAsync ~
    completed ~
    aggregate ~
    aggExpirations ~
    aggWarnings ~
    aggCount ~
    entityRoute ~
    entExpirations ~
    entWarnings ~
    entCount ~
    key ~
    keyCount ~
    tags

  // format: ON
}
