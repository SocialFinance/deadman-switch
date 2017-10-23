package org.sofi.deadman.http

import akka.http.scaladsl.common.EntityStreamingSupport
import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import spray.json._

trait JsonProtocol extends DefaultJsonProtocol {

  // Enable JSON streaming
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  // Command
  implicit val scheduleRequestJsonFormat = jsonFormat7(ScheduleRequest.apply)
  implicit val completeRequestJsonFormat = jsonFormat3(CompleteRequest.apply)
  implicit object CommandResponseJsonFormat extends RootJsonFormat[CommandResponse] {
    def write(rep: CommandResponse) =
      JsObject(Map("errors" -> JsArray(rep.errors.map(JsString.apply).toVector), "responseType" -> JsString(rep.responseType.name)))
    def read(json: JsValue) = throw new UnsupportedOperationException("read not supported")
  }

  // Event
  implicit val taskJsonFormat = jsonFormat7(Task.apply)

  // Query
  implicit val tasksJsonFormat = jsonFormat1(Tasks.apply)
}
