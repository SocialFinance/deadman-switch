package org.sofi.deadman.load

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._
import scala.util.Try

trait Profile { this: App ⇒

  // Load config
  private val profile = Try(args(0)).getOrElse("profile1")
  val config = ConfigFactory.load(profile)

  // HTTP service locations
  val ports = config.getIntList("ports").asScala.map(_.toInt)

  // Set ttl values on a 5 minute interval
  private val start = config.getInt("durations.start")
  private val end = config.getInt("durations.end")
  val durations = (start to end).filter(_ % 5 == 0).map(n => s"${n}min").toArray

  // Read load sizing
  val numAggregates = config.getInt("aggregates")
  val numEntities = config.getInt("entities")
  val groupSize = config.getInt("grouping")
}
