package org.sofi.deadman.load

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

// Complete some tasks in the deadman switch service
object Complete extends App {

  // HTTP service locations
  val ports = Array(9876, 9877, 9878)

  // Complete tasks for the given aggregate
  private def completeTasks(aggregates: Seq[Int]): Future[Unit] = Future {
    println(s"Completing tasks for aggregates: ${aggregates.mkString(" ")}")
    aggregates.foreach { a ⇒
      val tasks = (1 to NUM_ENTITIES).map { j ⇒
        Map[String, Any]("key" -> s"task$j", "aggregate" -> s"$a", "entity" -> s"${a - 1}")
      }
      val port = ports(a % ports.length)
      val rep = Http.post(s"http://127.0.0.1:$port/deadman/api/v1/complete", Json.encode(tasks))
      if (rep.status != Http.OK) {
        println(s"${rep.status}: ${rep.body}")
      }
    }
  }

  // Complete tasks for a range of aggregates
  def completeAggregates() =
    Future.sequence {
      (1 to NUM_AGGREGATES).grouped(10).map(completeTasks)
    }

  // Wait until all Futures are finished
  Await.result(completeAggregates(), 10.minutes)
  println("done!")

}
