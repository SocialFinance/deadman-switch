package org.sofi.deadman.client.async

import akka.http.scaladsl._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.sofi.deadman.client._
import org.sofi.deadman.client.req._
import org.sofi.deadman.messages.query._
import scala.concurrent._

final class AsyncClient(val host: Host) extends Client with JsonProtocol {

  private val SCHEDULE_URI = s"http://${host.host}:${host.port}/deadman/api/v1/schedule"
  private val COMPLETE_URI = s"http://${host.host}:${host.port}/deadman/api/v1/complete"

  private val http = Http(actorSystem)

  override def schedule(req: Seq[TaskReq])(implicit ec: ExecutionContext): Future[Tasks] =
    Marshal(req).to[RequestEntity] flatMap { entity ⇒
      val req = HttpRequest(method = HttpMethods.POST, uri = SCHEDULE_URI, entity = entity)
      http.singleRequest(req) flatMap { rep ⇒
        Unmarshal(rep.entity).to[Tasks]
      }
    }

  override def complete(req: Seq[CompleteReq])(implicit ec: ExecutionContext): Future[TaskTerminations] =
    Marshal(req).to[RequestEntity] flatMap { entity ⇒
      val req = HttpRequest(method = HttpMethods.POST, uri = COMPLETE_URI, entity = entity)
      http.singleRequest(req) flatMap { rep ⇒
        Unmarshal(rep.entity).to[TaskTerminations]
      }
    }
}

object AsyncClient {
  def apply(host: Host): AsyncClient = new AsyncClient(host)
}
