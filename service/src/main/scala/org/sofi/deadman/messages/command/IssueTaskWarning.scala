package org.sofi.deadman.messages.command

import org.sofi.deadman.messages.event.Task

/**
 * Task expiration warning command. This is local to the service because we don't want clients to be able to manually issue task
 * expiration warnings.
 */
final case class IssueTaskWarning(task: Task, ttw: Long)
