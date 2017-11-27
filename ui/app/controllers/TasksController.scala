package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.sofi.deadman.client._
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ AbstractController, ControllerComponents }

@Singleton
final class TasksController @Inject() (cc: ControllerComponents)(implicit system: ActorSystem) extends AbstractController(cc) {

  implicit val executionContext = cc.executionContext
  implicit val materializer = ActorMaterializer()
  implicit val client = Client()

  private def runQuery(id: String, label: String, query: Query) =
    query.exec().map { tasks ⇒
      Ok(views.html.tasks(id, label, tasks))
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
