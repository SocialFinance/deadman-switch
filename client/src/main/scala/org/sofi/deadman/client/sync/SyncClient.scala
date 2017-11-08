package org.sofi.deadman.client.sync

import org.sofi.deadman.client._
import org.sofi.deadman.client.req._
import org.sofi.deadman.messages.query._

final class SyncClient(val settings: Settings) extends Client[Result] {

  def schedule(req: Seq[TaskReq]): Result[Tasks] =
    new Result[Tasks] {
      // TODO
      val result = Tasks(Seq.empty)
    }

  def complete(req: Seq[CompleteReq]): Result[TaskTerminations] =
    new Result[TaskTerminations] {
      // TODO
      val result = TaskTerminations(Seq.empty)
    }
}

object SyncClient {
  def apply(settings: Settings): SyncClient = new SyncClient(settings)
}
