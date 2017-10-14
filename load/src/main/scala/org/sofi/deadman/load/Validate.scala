package org.sofi.deadman.load

// Validate that all tasks loaded into the deadman switch service expired
object Validate extends App {

  // Output status
  var ok = true

  // Validate aggregate expiration data
  (1 to 10).foreach { a ⇒
    val url = s"http://127.0.0.1:9876/deadman/api/v1/aggregate/$a/expirations"
    val resp = Http.get(url)
    if (resp.status == Http.OK) {
      val tasks = Json.decode(resp.body, "tasks", classOf[Seq[Map[Any, Any]]])
      val keys = tasks.map { task ⇒ task("key").toString }
      (1 to 100).foreach { k ⇒
        val taskKey = s"task$k"
        if (!keys.contains(taskKey)) {
          ok = false
          println(s"Key $taskKey not found for aggregate: $a")
        }
      }
    } else {
      println(s"${resp.status}: ${resp.body}")
    }
  }

  // Indicate when we get a clean run
  if (ok) {
    println("ok")
  }
}
