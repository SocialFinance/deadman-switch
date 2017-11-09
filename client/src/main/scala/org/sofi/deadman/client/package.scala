package org.sofi.deadman

//import akka.actor.ActorSystem
//import akka.stream.ActorMaterializer
//import com.typesafe.config.ConfigFactory

package object client {

  // The name of the remote log events are replicated from
  val eventLogName = "L1"

//  // Load and resolve config
//  val config = ConfigFactory.load().resolve()
//
//  // Create an actor system
//  implicit val actorSystem = ActorSystem("deadman-switch-actor-system", config)
//
//  // Put execution context in scope
//  implicit val executionContext = actorSystem.dispatcher
//
//  // Create an actor materializer for akka stream support
//  implicit val materializer = ActorMaterializer()
}
