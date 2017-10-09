package org.sofi.deadman.load

// Load 1 million tasks into the deadman switch service
object Main extends App {
  val ports = Array(9876, 9877, 9878) // Spread requests across all 3 service locations
  (1 to 10000).foreach { a ⇒
    val e = a - 1
    val s = System.currentTimeMillis() // Use a base start timestamp for each aggregate task
    (1 to 100).foreach { j ⇒
      val k = s"task$j"
      val x = if (j < 33) "2h" else if (j > 66) "4h" else "3h"
      val port = ports(j % ports.length)
      val url = s"http://127.0.0.1:$port/deadman/api/v1/task?k=$k&a=$a&e=$e&x=$x&s=${s + j}"
      println(url)
      val rep = Http.post(url)
      if (rep.status != Http.CREATED) {
        println(rep.body)
      }
    }
  }
}
