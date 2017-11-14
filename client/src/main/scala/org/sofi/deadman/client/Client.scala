package org.sofi.deadman.client

import akka.actor._
import akka.stream.ActorMaterializer
import org.sofi.deadman.client.async._
import org.sofi.deadman.client.req._
import org.sofi.deadman.messages.query._
import scala.concurrent._

// Deadman switch client
trait Client[M[_]] {

  // Submit a schedule request
  def schedule(req: Seq[TaskReq]): M[Tasks]

  // Submit a complete request
  def complete(req: Seq[CompleteReq]): M[TaskTerminations]

  // Query for tasks
  def tasks(query: Query): M[Tasks]
}

// Client factory
object Client {
  private val defSettings = new Settings{}
  def apply(settings: ⇒ Settings = defSettings)(implicit as: ActorSystem, am: ActorMaterializer): Client[Future] =
    AsyncClient(settings)
}
