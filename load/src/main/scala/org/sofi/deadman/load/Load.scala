package org.sofi.deadman.load

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

// Load some tasks into the deadman switch service
object Load extends App with Profile {

  // Schedule 100 tasks for the given aggregate
  private def scheduleTasks(aggregates: Seq[Int]): Future[Unit] = Future {
    println(s"Scheduling tasks for aggregates: ${aggregates.mkString(" ")}")
    aggregates.foreach { a ⇒
      val s = System.currentTimeMillis() // Use a base start timestamp for each aggregate task
      val tasks = (1 to numEntities).map { j ⇒
        val ts = s + j
        Map[String, Any](
          "key" -> s"task$j",
          "aggregate" -> s"$a",
          "entity" -> s"${a - 1}",
          "ttl" -> durations(a % durations.length),
          "ttw" -> Seq.empty,
          "tags" -> Seq.empty,
          "ts" -> ts
        )
      }
      val port = ports(a % ports.length)
      val rep = Http.post(s"http://127.0.0.1:$port/deadman/api/v1/schedule", Json.encode(tasks))
      if (rep.status != Http.CREATED) {
        println(s"${rep.status}: ${rep.body}")
      }
    }
  }

  // Schedule tasks for a range of aggregates
  def scheduleAggregates() =
    Future.sequence {
      (1 to numAggregates).grouped(groupSize).map(scheduleTasks)
    }

  // Wait until complete
  Await.result(scheduleAggregates(), 30.minutes)
  println("done!")
}
