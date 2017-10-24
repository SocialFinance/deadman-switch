package org.sofi.deadman.load

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

// Load some tasks into the deadman switch service
object Load extends App {

  // Schedule 100 tasks for the given aggregate
  private def scheduleTasks(a: Int): Future[Unit] = Future {
    val s = System.currentTimeMillis() // Use a base start timestamp for each aggregate task
    val tasks = (1 to NUM_ENTITIES).map { j ⇒
      val ts = s + j
      Map[String, Any](
        "key" -> s"task$j",
        "aggregate" -> s"$a",
        "entity" -> s"${a - 1}",
        "ttl" -> 10.minutes.toMillis,
        "ttw" -> Seq.empty,
        "tags" -> Seq.empty,
        "ts" -> ts
      )
    }
    println(s"Scheduling tasks for aggregate: $a")
    val rep = Http.post(s"http://127.0.0.1:9876/deadman/api/v1/schedule", Json.encode(tasks))
    if (rep.status != Http.CREATED) {
      println(s"${rep.status}: ${rep.body}")
    }
  }

  // Schedule tasks for a range of aggregates
  def scheduleAggregates() =
    Future.sequence {
      (1 to NUM_AGGREGATES).map(scheduleTasks)
    }

  // Wait until complete
  Await.result(scheduleAggregates(), 10.minutes)
  println("done!")
}
