package org.sofi.deadman.client.async

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.sofi.deadman.client.req._
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import spray.json._

trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  // Request
  implicit val taskReqJsonFormat = jsonFormat7(TaskReq)

  // Response
  implicit val taskJsonFormat = jsonFormat7(Task.apply)
  implicit val tasksJsonFormat = jsonFormat1(Tasks.apply)
}
