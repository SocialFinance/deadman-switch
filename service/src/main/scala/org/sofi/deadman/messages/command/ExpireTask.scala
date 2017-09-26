package org.sofi.deadman.messages.command

import org.sofi.deadman.messages.event.Task

/**
 * Task expiration command. This is local to the service because we don't want clients to be able to manually expire tasks.
 */
final case class ExpireTask(task: Task)
