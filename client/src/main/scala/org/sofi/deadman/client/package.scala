package org.sofi.deadman

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

package object client {

  // The name of the remote log where events are written
  val eventLogName = "L1"

  // Create an actor system
  implicit val actorSystem: ActorSystem = ActorSystem("deadman-switch-actor-system", ConfigFactory.load().resolve())

  // Create an actor materializer for akka stream support
  implicit val materializer = ActorMaterializer()
}
