package org.sofi.deadman.client.example

import org.sofi.deadman.client._
import org.sofi.deadman.client.req.TaskReq
import scala.concurrent._
import scala.concurrent.duration._

// Example deadman switch client `schedule` usage
object Schedule extends App {

  // Execution context
  final implicit val executionContext = actorSystem.dispatcher

  // Config
  val host = new Host { override val port = 9876 }
  val runID = System.currentTimeMillis()

  // Request
  val req = Seq(
    TaskReq("1", "1", s"task1$runID", "10s"),
    TaskReq("1", "2", s"task2$runID", "15s"),
    TaskReq("1", "3", s"task3$runID", "20s")
  )

  // Schedule some tasks that expire
  val tasksF = Client(host).schedule(req)
  tasksF.foreach(println)

  // Wait for the results
  Await.result(tasksF, 30.seconds)
  println("done!")
}
