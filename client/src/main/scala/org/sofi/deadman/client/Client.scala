package org.sofi.deadman.client

import org.sofi.deadman.client.async._
import org.sofi.deadman.client.req._
import org.sofi.deadman.client.sync._
import org.sofi.deadman.messages.query._
import scala.concurrent._

// Deadman switch client
trait Client[M[_]] {

  // Submit a schedule request
  def schedule(req: Seq[TaskReq]): M[Tasks]

  // Submit a complete request
  def complete(req: Seq[CompleteReq]): M[TaskTerminations]
}

// Client factory
object Client {
  def apply(settings: Settings): Client[Future] = AsyncClient(settings)
  def async(settings: Settings): Client[Future] = Client(settings)
  def sync(settings: Settings): Client[Result] = SyncClient(settings)
}
