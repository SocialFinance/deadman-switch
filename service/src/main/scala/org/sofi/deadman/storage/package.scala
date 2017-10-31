package org.sofi.deadman

import com.typesafe.config.ConfigFactory
import io.getquill._

package object storage {
  final val db = new CassandraAsyncContext[SnakeCase](ConfigFactory.load("writer.conf").resolve())
}
