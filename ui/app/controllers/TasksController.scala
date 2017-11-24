package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.sofi.deadman.client._
import javax.inject.Inject
import play.api.mvc.{ AbstractController, ControllerComponents }

final class TasksController @Inject() (cc: ControllerComponents)(implicit sys: ActorSystem) extends AbstractController(cc) {

  implicit val ec = cc.executionContext
  implicit val am = ActorMaterializer()
  implicit val client = Client()

  private def runQuery(id: String, label: String, query: Query) =
    query.exec().map { t ⇒
      Ok(views.html.tasks(id, label, t))
    }

  def tasks(id: String) = Action.async {
    runQuery(id, "Active Tasks", Query(id, Query.Aggregate))
  }

  def expirations(id: String) = Action.async {
    runQuery(id, "Expired Tasks", Query(id, Query.Aggregate, Query.Expired))
  }

  def warnings(id: String) = Action.async {
    runQuery(id, "Task Warnings", Query(id, Query.Aggregate, Query.Warning))
  }
}
