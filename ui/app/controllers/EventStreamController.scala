package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.sofi.deadman.client.stream._
import javax.inject.{ Inject, Singleton }
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.mvc._

@Singleton
final class EventStreamController @Inject() (cc: ControllerComponents)(implicit system: ActorSystem) extends AbstractController(cc) {

  implicit val materializer = ActorMaterializer()
  private val settings = new StreamSettings { val id = "event-stream-controller" }
  private val source = EventStream(settings).jsonEvents

  def index() = Action {
    Ok(views.html.sink())
  }

  def events() = Action {
    Ok.chunked(source via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }
}
