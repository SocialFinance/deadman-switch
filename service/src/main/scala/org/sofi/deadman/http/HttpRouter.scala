package org.sofi.deadman.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._

class HttpRouter(implicit api: ApiFunctions) extends JsonProtocol {
  import api._

  // format: OFF

  private val schedule =
    pathPrefix("deadman" / "api" / "v1" / "task") {
      pathEndOrSingleSlash {
        post {
          parameters('k.as[String], 'a.as[String], 'e.as[String], 'x.as[Long], 'w.as[String].?, 't.as[String].?, 's.as[Long].?) {
            (key, agg, ent, ttl, ttw, tags, ts) ⇒
              onSuccess(scheduleTask(key, agg, ent, ttl, ttw, tags, ts)) { resp ⇒
                complete(resp)
              }
          }
        }
      }
    }

  private val completed =
    pathPrefix("deadman" / "api" / "v1" / "task") {
      pathEndOrSingleSlash {
        put {
          parameters('k.as[String], 'a.as[String], 'e.as[String]) {
            (key, agg, ent) ⇒
              onSuccess(completeTask(key, agg, ent)) { resp ⇒
                complete(resp)
              }
          }
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

  private val entity =
    pathPrefix("deadman" / "api" / "v1" / "entity" / Segment) { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryEntity(id)) { tasks ⇒
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

  private val tags =
    pathPrefix("deadman" / "api" / "v1" / "tag" / Segment) { tag ⇒
      path(Segment) { window =>
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
    aggregate ~
    entity ~
    aggExpirations ~
    aggWarnings ~
    aggCount ~
    entExpirations ~
    entWarnings ~
    entCount ~
    tags

  // format: ON
}
