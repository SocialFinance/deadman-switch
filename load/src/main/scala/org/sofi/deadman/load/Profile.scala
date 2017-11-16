package org.sofi.deadman.load

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._
import scala.util.Try

trait Profile { this: App ⇒

  // Load config
  private lazy val profile = Try(args(0)).getOrElse("profile0")
  lazy val config = ConfigFactory.load(profile)

  // HTTP service locations
  lazy val ports = config.getIntList("ports").asScala.map(_.toInt)

  // Set ttl values on a 5 minute interval
  private lazy val start = config.getInt("durations.start")
  private lazy val end = config.getInt("durations.end")
  lazy val durations = (start to end).filter(_ % 5 == 0).map(n ⇒ s"${n}min").toArray

  // Read load sizing
  lazy val numAggregates = config.getInt("aggregates")
  lazy val numEntities = config.getInt("entities")
  lazy val groupSize = config.getInt("grouping")
}
