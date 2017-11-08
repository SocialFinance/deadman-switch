package org.sofi.deadman.client
package async

import akka.http.scaladsl._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.sofi.deadman.client._
import org.sofi.deadman.client.req._
import org.sofi.deadman.messages.query._
import scala.concurrent._

final class AsyncClient(val settings: Settings) extends Client[Future] with JsonProtocol {

  def schedule(req: Seq[TaskReq]): Future[Tasks] =
    Marshal(req).to[RequestEntity] flatMap { entity ⇒
      val scheduleUri = s"http://${settings.host}:${settings.port}/deadman/api/v1/schedule"
      val req = HttpRequest(method = HttpMethods.POST, uri = scheduleUri, entity = entity)
      Http().singleRequest(req) flatMap { rep ⇒
        Unmarshal(rep.entity).to[Tasks]
      }
    }

  def complete(req: Seq[CompleteReq]): Future[TaskTerminations] =
    Marshal(req).to[RequestEntity] flatMap { entity ⇒
      val completeUri = s"http://${settings.host}:${settings.port}/deadman/api/v1/complete"
      val req = HttpRequest(method = HttpMethods.POST, uri = completeUri, entity = entity)
      Http().singleRequest(req) flatMap { rep ⇒
        Unmarshal(rep.entity).to[TaskTerminations]
      }
    }
}

object AsyncClient {
  def apply(settings: Settings): AsyncClient = new AsyncClient(settings)
}
