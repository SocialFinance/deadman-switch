package org.sofi.deadman.load

import scala.concurrent.duration._

// Load some tasks into the deadman switch service using the JSON stream endpoint
object Load extends App {

  // Schedule 100 tasks for the given aggregate
  private def scheduleTasks(a: Int): Unit = {
    val s = System.currentTimeMillis() // Use a base start timestamp for each aggregate task
    val tasks = (1 to 100).map { j ⇒
      Json.encode(
        Map[String, Any](
          "key" -> s"task$j",
          "aggregate" -> s"$a",
          "entity" -> s"${a - 1}",
          "ttl" -> (if (a < 33) 3.minutes else if (a > 66) 2.minutes else 1.minute).toMillis,
          "ttw" -> Seq.empty,
          "tags" -> Seq.empty,
          "ts" -> s
        )
      )
    }
    println(s"Scheduling tasks for aggregate $a")
    val rep = Http.post("http://127.0.0.1:9876/deadman/api/v1/task", tasks.mkString("\n"))
    println(rep.body)
  }

  // Schedule tasks for a range of aggregates
  (1 to 10).foreach(scheduleTasks)

  // Wait until complete
  println("done!")
}
