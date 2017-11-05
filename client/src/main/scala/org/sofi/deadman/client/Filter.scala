package org.sofi.deadman.client

trait Filter {
  def apply(any: Any): Boolean = true
}
