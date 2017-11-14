package org.sofi.deadman.client
package async

import akka.actor._
import akka.http.scaladsl._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.sofi.deadman.client.req._
import org.sofi.deadman.messages.query._
import scala.concurrent._

final class AsyncClient(val settings: Settings)(implicit val sys: ActorSystem, am: ActorMaterializer) extends Client[Future] with JsonProtocol {

  private implicit val executionContext = sys.dispatcher

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

  def tasks(query: Query): Future[Tasks] = {
    val uri = s"http://${settings.host}:${settings.port}/deadman/api/v1/${query.uri}"
    Http().singleRequest(HttpRequest(uri = uri)) flatMap { rep ⇒
      Unmarshal(rep.entity).to[Tasks]
    }
  }
}

object AsyncClient {
  def apply(settings: Settings)(implicit system: ActorSystem, am: ActorMaterializer): AsyncClient = new AsyncClient(settings)
}
