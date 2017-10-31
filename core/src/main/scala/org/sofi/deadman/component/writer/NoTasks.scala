package org.sofi.deadman.component.writer

import akka.actor.ActorLogging
import org.sofi.deadman.messages.query.Tasks
import scala.concurrent.Future
import scala.util.control.NonFatal

trait NoTasks { this: ActorLogging ⇒
  def noTasks: PartialFunction[Throwable, Future[Tasks]] = {
    case NonFatal(error) ⇒
      log.warning("Task query exception: {}", error)
      Future.successful(Tasks(Seq.empty))
  }
}
