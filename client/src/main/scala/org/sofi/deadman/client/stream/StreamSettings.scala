package org.sofi.deadman.client
package stream

trait StreamSettings extends Settings {
  def id: String
  def offset: Option[Long] = None
  def aggregate: Option[String] = None
  def filter: EventFilter = new EventFilter {}
}
