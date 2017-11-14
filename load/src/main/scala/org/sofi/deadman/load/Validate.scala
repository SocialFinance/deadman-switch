package org.sofi.deadman.load

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.sofi.deadman.client._
import scala.concurrent._
import scala.concurrent.duration._

// Validate that all tasks loaded into the deadman switch service expired
object Validate extends App with Profile {

  // Create an actor system
  implicit val actorSystem = ActorSystem("validate-actor-system", config)
  implicit val executionContext = actorSystem.dispatcher
  implicit val materializer = ActorMaterializer()

  // Create client
  val settings = new Settings {
    override val port = scala.util.Random.shuffle(ports).head
  }
  val client = Client(settings)

  // Output status
  var ok = true

  // Validate aggregate expiration data
  (1 to numAggregates).foreach { a ⇒
    val keys = Await.result(client.expirations(s"$a").map(_.tasks.map(_.key)), 10.seconds)
    (1 to numEntities).foreach { k ⇒
      val taskKey = s"task$k"
      if (!keys.contains(taskKey)) {
        ok = false
        println(s"Key $taskKey not found for aggregate: $a")
      }
    }
  }

  // Indicate when we get a clean run
  if (ok) {
    println("ok")
  }
  actorSystem.terminate()
}
