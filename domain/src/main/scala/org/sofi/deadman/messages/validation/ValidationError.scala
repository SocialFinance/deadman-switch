package org.sofi.deadman.messages.validation

sealed trait ValidationError {
  def error: String
}

final case class InvalidString(field: String) extends ValidationError {
  val error = s"$field must be a non-empty string"
}

case object InvalidKey extends ValidationError {
  val error = "key must be a non-empty string consisting of upper-case chars, lower-case chars and numbers"
}

case object InvalidTTL extends ValidationError {
  val error = "ttl must be a valid duration >= 1 second"
}

case object InvalidTTW extends ValidationError {
  val error = "ttw values must be valid durations >= 1 second and < ttl"
}

case object InvalidTags extends ValidationError {
  val error = "tags must be lower-case strings without spaces or special characters"
}

case object InvalidTimestamp extends ValidationError {
  val error = "timestamp must be > 0"
}
