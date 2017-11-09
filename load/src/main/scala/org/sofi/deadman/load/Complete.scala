package org.sofi.deadman.load

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.sofi.deadman.client._
import org.sofi.deadman.client.req._
import scala.concurrent._
import scala.concurrent.duration._

// Complete some tasks in the deadman switch service
object Complete extends App with Profile {

  // Create an actor system
  implicit val actorSystem = ActorSystem("complete-actor-system", config)
  implicit val executionContext = actorSystem.dispatcher
  implicit val materializer = ActorMaterializer()

  // Create client
  val settings = new Settings {
    override val port = scala.util.Random.shuffle(ports).head
  }
  val client = Client(settings)

  // Complete tasks for a range of aggregates
  (1 to numAggregates).grouped(groupSize).foreach { aggregates ⇒
    println(s"Completing tasks for aggregates: ${aggregates.mkString(" ")}")
    val completed = Future.sequence {
      aggregates.map { a ⇒
        val reqs = (1 to numEntities).map(k ⇒ CompleteReq(s"$a", s"${a - 1}", s"task$k"))
        client.complete(reqs)
      }
    }
    Await.result(completed, 5.minutes)
  }
  println("done!")
  actorSystem.terminate()
}
