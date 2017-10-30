package org.sofi.deadman.load

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

// Load some tasks into the deadman switch service
object Load extends App {

  // HTTP service locations
  val ports = Array(9876, 9877, 9878)

  // Pick a ttl on a 5 minute interval between 30 minutes and 2 hours.
  val durations = (30 to 120).filter(_ % 5 == 0).map(_.minutes.toMillis).toArray

  // Schedule 100 tasks for the given aggregate
  private def scheduleTasks(aggregates: Seq[Int]): Future[Unit] = Future {
    println(s"Scheduling tasks for aggregates: ${aggregates.mkString(" ")}")
    aggregates.foreach { a ⇒
      val s = System.currentTimeMillis() // Use a base start timestamp for each aggregate task
      val tasks = (1 to NUM_ENTITIES).map { j ⇒
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
      (1 to NUM_AGGREGATES).grouped(10).map(scheduleTasks)
    }

  // Wait until complete
  Await.result(scheduleAggregates(), 30.minutes)
  println("done!")
}
