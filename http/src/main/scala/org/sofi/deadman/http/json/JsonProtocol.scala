package org.sofi.deadman.http.json

import akka.http.scaladsl.common.EntityStreamingSupport
import org.sofi.deadman.http.request.{ CompleteRequest, ScheduleRequest }
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import spray.json._

trait JsonProtocol extends DefaultJsonProtocol {

  // Enable JSON streaming
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  // Requests
  implicit val scheduleRequestJsonFormat = jsonFormat7(ScheduleRequest.apply)
  implicit val completeRequestJsonFormat = jsonFormat3(CompleteRequest.apply)

  // Event
  implicit val taskJsonFormat = jsonFormat7(Task.apply)

  // Query
  implicit val tasksJsonFormat = jsonFormat1(Tasks.apply)
}
