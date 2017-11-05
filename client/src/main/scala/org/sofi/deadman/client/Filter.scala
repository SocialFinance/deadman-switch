package org.sofi.deadman.client

trait Filter { self ⇒
  def apply(any: Any): Boolean = true
  def &(other: Filter) = new Filter { override def apply(any: Any): Boolean = self(any) && other(any) }
  def |(other: Filter) = new Filter { override def apply(any: Any): Boolean = if (self(any)) true else other(any) }
}
