package org.sofi.deadman.http

import org.sofi.deadman.messages.command._
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import spray.json._

trait JsonProtocol extends DefaultJsonProtocol {

  // Command
  implicit object CommandResponseJsonFormat extends RootJsonFormat[CommandResponse] {
    def write(rep: CommandResponse) = JsObject(Map("body" -> JsString(rep.body), "responseType" -> JsString(rep.responseType.name)))
    def read(json: JsValue) = throw new UnsupportedOperationException("read not supported")
  }

  // Event
  implicit val taskJsonFormat = jsonFormat7(Task.apply)

  // Query
  implicit val tasksJsonFormat = jsonFormat1(Tasks.apply)
}
