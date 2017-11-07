package org.sofi.deadman.client.example

import org.sofi.deadman.client._, req._
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

  // Create client
  val client = Client(host)

  // Schedule some tasks that expire
  client.schedule(req).onSuccess {
    case result: Any ⇒
      println(result)
      // Complete one of the tasks and wait for that result
      val completeF = client.complete(Seq(CompleteReq("1", "3", s"task3$runID")))
      completeF.foreach(println)
      Await.result(completeF, 30.seconds)
  }
}
