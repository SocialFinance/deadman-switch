package org.sofi.deadman.http.router

import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import org.sofi.deadman.http.api._
import org.sofi.deadman.http.json._
import org.sofi.deadman.http.request._

final class HttpRouter(implicit query: QueryApi, stream: StreamApi, am: ActorMaterializer) extends JsonProtocol {
  import query._, stream._

  // format: OFF

  private val schedule =
    path("deadman" / "api" / "v1" / "schedule") {
      entity(asSourceOf[ScheduleRequest]) { source ⇒
        onSuccess(scheduleTasks(source)) { tasks ⇒
          complete(Created -> tasks)
        }
      }
    }

  private val completed =
    path("deadman" / "api" / "v1" / "complete") {
      entity(asSourceOf[CompleteRequest]) { source ⇒
        onSuccess(completeTasks(source)) { count ⇒
          complete(OK -> Map("completed" -> count))
        }
      }
    }

  private val aggregates =
    pathPrefix("deadman" / "api" / "v1" / "aggregate" / Segment) { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryAggregate(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val expirations =
    pathPrefix("deadman" / "api" / "v1" / "aggregate" / Segment / "expirations") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryAggregateExpirations(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val warnings =
    pathPrefix("deadman" / "api" / "v1" / "aggregate" / Segment / "warnings") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryAggregateWarnings(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val entities =
    pathPrefix("deadman" / "api" / "v1" / "entity" / Segment) { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryEntity(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val entityExpirations =
    pathPrefix("deadman" / "api" / "v1" / "entity" / Segment / "expirations") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryEntityExpirations(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val entityWarnings =
    pathPrefix("deadman" / "api" / "v1" / "entity" / Segment / "warnings") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryEntityWarnings(id)) { tasks ⇒
            complete(tasks)
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

  private val keyExpirations =
    pathPrefix("deadman" / "api" / "v1" / "key" / Segment / "expirations") { key ⇒
      path(Segment) { window ⇒
        pathEndOrSingleSlash {
          get {
            onSuccess(queryExpiredKey(key, window)) { tasks ⇒
              complete(tasks)
            }
          }
        }
      }
    }

  private val tags =
    pathPrefix("deadman" / "api" / "v1" / "tag" / Segment / "expirations") { tag ⇒
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
    completed ~
    aggregates ~
    expirations ~
    warnings ~
    entities ~
    entityExpirations ~
    entityWarnings ~
    key ~
    keyExpirations ~
    tags

  // format: ON
}
