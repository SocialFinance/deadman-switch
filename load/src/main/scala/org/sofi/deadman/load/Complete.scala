package org.sofi.deadman.load

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

// Complete some tasks in the deadman switch service
object Complete extends App {

  // Complete tasks for the given aggregate
  private def completeTasks(a: Int): Future[Unit] = Future {
    val tasks = (1 to NUM_ENTITIES).map { j ⇒
      Map[String, Any]("key" -> s"task$j", "aggregate" -> s"$a", "entity" -> s"${a - 1}")
    }
    println(s"Completing tasks for aggregate: $a")
    val rep = Http.post(s"http://127.0.0.1:9876/deadman/api/v1/complete", Json.encode(tasks))
    if (rep.status != Http.OK) {
      println(s"${rep.status}: ${rep.body}")
    }
  }

  // Complete tasks for a range of aggregates
  def completeAggregates() =
    Future.sequence {
      (1 to NUM_AGGREGATES).map(completeTasks)
    }

  // Wait until all Futures are finished
  Await.result(completeAggregates(), 10.minutes)
  println("done!")

}
