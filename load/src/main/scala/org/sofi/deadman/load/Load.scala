package org.sofi.deadman.load

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

// Load some tasks into the deadman switch service
object Load extends App {

  private val ports = Array(9876) //, 9877, 9878)
  private val maxBackoff = 2.seconds
  private val backoff = 250.millis

  // HTTP post with retry logic
  @tailrec
  private def post(url: String, pause: Duration = 1.second, max: Int = 10, n: Int = 0): Http.HttpResp = {
    println(url)
    val resp = Http.post(url)
    // Only retry on 503s
    if (resp.status != Http.SERVICE_UNAVAILABLE || n >= max) resp else {
      Thread.sleep(pause.toMillis)
      val nextPause = if (pause + backoff > maxBackoff) maxBackoff else pause + backoff
      post(url, nextPause, max, n + 1)
    }
  }

  // Schedule 100 tasks for the given aggregate
  private def scheduleTasks(a: Int): Future[Unit] = Future {
    val e = a - 1
    val s = System.currentTimeMillis() // Use a base start timestamp for each aggregate task
    (1 to 100).foreach { j ⇒
      val k = s"task$j"
      val x = if (a < 33) "20min" else if (a > 66) "10min" else "15min"
      val port = ports(j % ports.length)
      val rep = post(s"http://127.0.0.1:$port/deadman/api/v1/task/async?k=$k&a=$a&e=$e&x=$x&s=${s + j}")
      if (rep.status != Http.OK) {
        println(s"${rep.status}: ${rep.body}")
      }
    }
  }

  // Schedule tasks for a range of aggregates
  def scheduleAggregates() =
    Future.sequence {
      (1 to 100).map(scheduleTasks)
    }

  // Wait until complete
  Await.result(scheduleAggregates(), 10.minutes)
  println("done!")
}
