package org.sofi.deadman.client.sync

trait Result[T] {
  def result: T
}
