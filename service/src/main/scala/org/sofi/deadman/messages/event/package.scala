package org.sofi.deadman.messages

package object event {

  def uid(aggregate: String, entity: String, key: String): String = s"$aggregate:$entity:$key"

  implicit class TaskOps(val t: Task) extends AnyVal {
    def id: String = uid(t.aggregate, t.entity, t.key)
    def isExpired: Boolean = t.ts + t.ttl < System.currentTimeMillis()
  }

  implicit class TaskTerminationOps(val t: TaskTermination) extends AnyVal {
    def id: String = uid(t.aggregate, t.entity, t.key)
  }
}
