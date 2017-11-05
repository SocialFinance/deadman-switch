package org.sofi.deadman.client

trait Settings {
  def id: String
  def host: String = "127.0.0.1"
  def port: Int = 2551
  def offset: Option[Long] = None
  def aggregate: Option[String] = None
  def filter: Filter = new Filter {}
}
