package org.sofi.deadman.client.async

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.sofi.deadman.client.req._
import org.sofi.deadman.messages.event._
import org.sofi.deadman.messages.query._
import spray.json._

trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  // Request
  implicit val taskReqJsonFormat = jsonFormat7(TaskReq)
  implicit val completeReqJsonFormat = jsonFormat3(CompleteReq)

  // Response
  implicit val taskJsonFormat = jsonFormat7(Task.apply)
  implicit val tasksJsonFormat = jsonFormat1(Tasks.apply)
  implicit val taskTerminationJsonFormat = jsonFormat3(TaskTermination.apply)
  implicit val taskTerminationsJsonFormat = jsonFormat1(TaskTerminations.apply)
}
