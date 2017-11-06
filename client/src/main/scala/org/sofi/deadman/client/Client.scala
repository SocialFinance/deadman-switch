package org.sofi.deadman.client

import org.sofi.deadman.client.async.AsyncClient
import org.sofi.deadman.client.req._
import org.sofi.deadman.messages.query._
import scala.concurrent._

// Deadman switch client
trait Client {

  // Submit a schedule request
  def schedule(req: Seq[TaskReq])(implicit ec: ExecutionContext): Future[Tasks]

  // Submit a complete request
  def complete(req: Seq[CompleteReq])(implicit ec: ExecutionContext): Future[Int]
}

object Client {
  def apply(host: Host): Client = AsyncClient(host)
}
