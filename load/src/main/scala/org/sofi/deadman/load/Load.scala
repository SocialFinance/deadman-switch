package org.sofi.deadman.load

// Load some tasks into the deadman switch service
object Load extends App {
  val ports = Array(9876) //, 9877, 9878)
  (1 to 10).foreach { a ⇒
    val e = a - 1
    val s = System.currentTimeMillis() // Use a base start timestamp for each aggregate task
    (1 to 100).foreach { j ⇒
      val k = s"task$j"
      val x = if (j < 33) "3min" else if (j > 66) "1min" else "2min"
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
