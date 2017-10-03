package org.sofi.deadman.load

// Load 1 million tasks into the deadman switch service
object Main extends App {
  val x1 = 7200000L  // 2 hr
  val x2 = 10800000L // 3 hr
  val x3 = 14400000L // 4 hr
  (1 to 10000).foreach { a ⇒
    val e = a - 1
    (1 to 100).foreach { j ⇒
      val k = s"task$j"
      val x = if (j < 33) x1 else if (j > 66) x3 else x2
      val url = s"http://127.0.0.1:9876/deadman/api/v1/task?k=$k&a=$a&e=$e&x=$x"
      println(url)
      println(Http.post(url).body)
    }
  }
}
