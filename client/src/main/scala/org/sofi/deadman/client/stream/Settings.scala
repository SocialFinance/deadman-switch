package org.sofi.deadman.client.stream

import org.sofi.deadman.client.Host

trait Settings extends Host {
  def id: String
  def offset: Option[Long] = None
  def aggregate: Option[String] = None
  def filter: Filter = new Filter {}
}
