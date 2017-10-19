package org.sofi.deadman.load

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

// Load some tasks into the deadman switch service using the async endpoint
object LoadAsync extends App {

  private val ports = Array(9876) //, 9877, 9878)
  private val maxBackoff = 2.seconds
  private val backoff = 250.millis

  // HTTP post with retry logic
  @tailrec
  private def post(url: String, body: String, pause: Duration = 1.second, max: Int = 10, n: Int = 0): Http.HttpResp = {
    println(url)
    val resp = Http.post(url, body)
    // Only retry on 503s
    if (resp.status != Http.SERVICE_UNAVAILABLE || n >= max) resp else {
      Thread.sleep(pause.toMillis)
      val nextPause = if (pause + backoff > maxBackoff) maxBackoff else pause + backoff
      post(url, body, nextPause, max, n + 1)
    }
  }

  // Schedule 100 tasks for the given aggregate
  private def scheduleTasks(a: Int): Future[Unit] = Future {
    val s = System.currentTimeMillis() // Use a base start timestamp for each aggregate task
    (1 to 100).foreach { j ⇒
      val port = ports(j % ports.length)
      val task = Json.encode(
        Map[String, Any](
          "key" -> s"task$j",
          "aggregate" -> s"$a",
          "entity" -> s"${a-1}",
          "ttl" -> (if (a < 33) 3.minutes else if (a > 66) 2.minutes else 1.minute).toMillis,
          "ttw" -> Seq.empty,
          "tags" -> Seq.empty,
          "ts" -> s
        )
      )
      val rep = post(s"http://127.0.0.1:$port/deadman/api/v1/task/async", task)
      if (rep.status != Http.OK) {
        println(s"${rep.status}: ${rep.body}")
      }
    }
  }

  // Schedule tasks for a range of aggregates
  def scheduleAggregates() =
    Future.sequence {
      (1 to 10).map(scheduleTasks)
    }

  // Wait until complete
  Await.result(scheduleAggregates(), 10.minutes)
  println("done!")
}
