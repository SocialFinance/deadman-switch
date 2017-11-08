package org.sofi.deadman.client
package example

import org.sofi.deadman.client.req._
import org.sofi.deadman.messages.query._
import scala.concurrent._
import scala.concurrent.duration._

// Example deadman switch client `schedule` and `complete` usage
object Schedule extends App {

  // Config
  val settings = new Settings { override val port = 9876 }
  val runID = System.currentTimeMillis()

  // Request
  val req = Seq(
    TaskReq("1", "1", s"task1$runID", "10s"),
    TaskReq("1", "2", s"task2$runID", "15s"),
    TaskReq("1", "3", s"task3$runID", "20s")
  )

  // Create client
  val client = Client(settings)

  // Schedule some tasks that expire
  client.schedule(req).onSuccess {
    case Tasks(tasks) ⇒
      println(tasks)
      // Complete one of the tasks and wait for that result
      val completeF = client.complete(Seq(CompleteReq("1", "3", s"task3$runID")))
      completeF.foreach(println)
      Await.result(completeF, 30.seconds)
  }
}
