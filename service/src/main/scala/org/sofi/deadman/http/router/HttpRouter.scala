package org.sofi.deadman.http.router

import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import org.sofi.deadman.http.api._
import org.sofi.deadman.http.json._
import org.sofi.deadman.http.request._
import org.sofi.deadman.messages.command._, ResponseType.SUCCESS

final class HttpRouter(implicit command: CommandApi, query: QueryApi, stream: StreamApi, am: ActorMaterializer) extends JsonProtocol {
  import command._, query._, stream._

  // format: OFF

  private val schedule =
    path("deadman" / "api" / "v1" / "schedule") {
      entity(asSourceOf[ScheduleRequest]) { source ⇒
        onSuccess(scheduleTasks(source)) {
          case Left(error) ⇒ complete(BadRequest -> Map("errors" -> error.split("\n")))
          case Right(tasks) ⇒ complete(Created -> tasks)
        }
      }
    }

  private val completed =
    path("deadman" / "api" / "v1" / "complete") {
      entity(asSourceOf[CompleteRequest]) { source ⇒
        onSuccess(completeTasks(source)) {
          case Left(error) ⇒ complete(BadRequest -> Map("errors" -> error.split("\n")))
          case Right(terminated) ⇒ complete(terminated)
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

  private val snapshotEp =
    pathPrefix("deadman" / "api" / "v1" / "aggregate" / Segment / "snapshot") { id ⇒
      pathEndOrSingleSlash {
        post {
          onSuccess(snapshot(id)) { resp ⇒
            val status = if (resp.responseType == SUCCESS) Created else BadRequest
            complete(status -> Map("error" -> resp.errors))
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
    tags ~
    snapshotEp

  // format: ON
}
