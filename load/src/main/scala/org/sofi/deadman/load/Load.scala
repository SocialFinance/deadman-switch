package org.sofi.deadman.load

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.sofi.deadman.client._
import org.sofi.deadman.client.req._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

// Load some tasks into the deadman switch service
object Load extends App with Profile {

  // Create an actor system
  implicit val actorSystem = ActorSystem("load-actor-system", config)
  implicit val materializer = ActorMaterializer()

  // Create client
  val settings = new Settings { override val port = scala.util.Random.shuffle(ports).head }
  val client = Client(settings)

  // Schedule tasks for the given aggregates
  private def scheduleTasks(aggregates: Seq[Int]) = {
    println(s"Scheduling tasks for aggregates: ${aggregates.mkString(" ")}")
    val tasks = Future.sequence {
      aggregates.map { a ⇒
        val reqs = (1 to numEntities).map { k ⇒
          TaskReq(s"$a", s"${a - 1}", s"task$k", durations(a % durations.length), System.currentTimeMillis() + k)
        }
        client.schedule(reqs)
      }
    }
    Await.result(tasks, 5.minutes)
  }

  // Schedule tasks for a range of aggregates
  (1 to numAggregates).grouped(groupSize).foreach(scheduleTasks)

  // Wait until complete
  println("done!")
  actorSystem.terminate()
}
