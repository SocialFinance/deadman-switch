package org.sofi.deadman.load

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.sofi.deadman.client._, Query._
import scala.concurrent._
import scala.concurrent.duration._

object AggQuery extends App with Profile {

  // Create an actor system
  implicit val actorSystem = ActorSystem("query-tasks-actor-system", config)
  implicit val executionContext = actorSystem.dispatcher
  implicit val materializer = ActorMaterializer()

  // Create client
  val settings = new Settings {
    override val port = scala.util.Random.shuffle(ports).head
  }
  val client = Client(settings)

  // Query active tasks by aggregate ID
  (1 to numAggregates).foreach { a ⇒
    Await.result(client.tasks(Query(s"$a", Aggregate)), 10.seconds)
      .tasks.foreach(task ⇒ println(s"${task.aggregate} ${task.entity} ${task.key}"))
  }

  // Done
  actorSystem.terminate()
}
