package org.sofi.deadman.messages

package object query {

  // Determine the `GetTask` query key field based on type
  implicit class GetTasksOps(val gt: GetTasks) extends AnyVal {
    def queryKey: Option[String] =
      gt.queryType match {
        case QueryType.AGGREGATE ⇒ gt.aggregate
        case QueryType.ENTITY ⇒ gt.entity
        case QueryType.KEY ⇒ gt.key
        case _ ⇒ gt.aggregate
      }
  }
}
