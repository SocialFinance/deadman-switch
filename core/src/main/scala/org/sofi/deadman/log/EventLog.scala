package org.sofi.deadman.log

sealed trait EventLog {
  def name: String
}

case object EventLog extends EventLog {
  val name = "L1"
}

case object TagLog extends EventLog {
  val name = "T1"
}

case object KeyLog extends EventLog {
  val name = "K1"
}
