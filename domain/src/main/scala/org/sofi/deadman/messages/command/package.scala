package org.sofi.deadman.messages

import org.sofi.deadman.messages.event.{ Task, TaskTermination }

package object command {

  implicit class ScheduleTaskOps(val st: ScheduleTask) extends AnyVal {
    def event = Task(st.key, st.aggregate, st.entity, st.ts.getOrElse(System.currentTimeMillis()), st.ttl, st.ttw, st.tags)
  }

  implicit class CompleteTaskOps(val ct: CompleteTask) extends AnyVal {
    def event = TaskTermination(ct.key, ct.aggregate, ct.entity)
  }
}
