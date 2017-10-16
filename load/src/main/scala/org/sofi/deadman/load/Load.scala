package org.sofi.deadman.load

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

// Load some tasks into the deadman switch service
object Load extends App {

  private val ports = Array(9876, 9877, 9878)

  private def scheduleTasks(a: Int): Future[Unit] = Future {
    val e = a - 1
    val s = System.currentTimeMillis() // Use a base start timestamp for each aggregate task
    (1 to 100).foreach { j ⇒
      val k = s"task$j"
      val x = s"${a}min"
      val port = ports(j % ports.length)
      val url = s"http://127.0.0.1:$port/deadman/api/v1/task/async?k=$k&a=$a&e=$e&x=$x&s=${s + j}"
      println(url)
      val rep = Http.post(url)
      if (rep.status != Http.OK) {
        println(rep.body)
      }
    }
  }

  def scheduleAggregates() =
    Future.sequence {
      (1 to 10).map(scheduleTasks)
    }

  Await.result(scheduleAggregates(), 10.minutes)
  println("done!")
}
