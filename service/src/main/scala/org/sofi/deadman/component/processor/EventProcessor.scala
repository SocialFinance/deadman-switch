package org.sofi.deadman.component.processor

import com.rbmhtechnology.eventuate.EventsourcedProcessor

private[processor] trait EventProcessor extends EventsourcedProcessor {
  def onCommand: Receive = {
    case _ ⇒
  }
}
