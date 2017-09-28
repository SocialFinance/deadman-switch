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
          parameters('k.as[String], 'a.as[String], 'e.as[String], 'w.as[String], 'x.as[Long], 't.as[String]) {
            (key, agg, ent, ttw, ttl, tags) ⇒
              onSuccess(scheduleTask(key, agg, ent, ttw, ttl, tags)) { resp ⇒
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

  private val expirations =
    pathPrefix("deadman" / "api" / "v1" / "aggregate" / Segment / "expiration") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryAggregateExpirations(id)) { tasks ⇒
            complete(tasks)
          }
        }
      }
    }

  private val warnings =
    pathPrefix("deadman" / "api" / "v1" / "aggregate" / Segment / "warning") { id ⇒
      pathEndOrSingleSlash {
        get {
          onSuccess(queryAggregateWarnings(id)) { tasks ⇒
            complete(tasks)
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

  val routes = schedule ~ completed ~ aggregate ~ entity ~ expirations ~ warnings ~ tags

  // format: ON
}
