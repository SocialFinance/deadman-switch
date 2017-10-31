package org.sofi.deadman.test

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import com.typesafe.config.ConfigFactory
import org.scalatest.{ MustMatchers, WordSpecLike }
import org.sofi.deadman.component.actor.TaskActor
import scala.util.Random

// Custom akka testing base class
abstract class TestSystem extends TestKit(ActorSystem("test-actor-system", ConfigFactory.load("test")))
  with WordSpecLike with MustMatchers with ImplicitSender with SystemTermination {

  // Generate a random aggregate ID for testing
  val aggregate: String = Random.alphanumeric.take(5).mkString("")

  // LevelDB event log (local file-system)
  val eventLog = system.actorOf(LeveldbEventLog.props(aggregate))

  // Command actor
  val taskActor = system.actorOf(TaskActor.props(aggregate, "test", eventLog))
}
