package org.sofi.deadman.messages.validation

sealed trait DomainValidation {
  def error: String
}

final case class InvalidString(field: String) extends DomainValidation {
  val error = s"$field must be a non-empty string"
}

case object InvalidKey extends DomainValidation {
  val error = "key must be a non-empty string consisting of upper-case chars, lower-case chars and numbers"
}

case object InvalidTTL extends DomainValidation {
  val error = "ttl must be a valid duration >= 1 second"
}

case object InvalidTTW extends DomainValidation {
  val error = "ttw values must be valid durations >= 1 second and < ttl"
}

case object InvalidTags extends DomainValidation {
  val error = "tags must be lower-case strings without spaces or special characters"
}

case object InvalidTimestamp extends DomainValidation {
  val error = "timestamp must be > 0"
}
