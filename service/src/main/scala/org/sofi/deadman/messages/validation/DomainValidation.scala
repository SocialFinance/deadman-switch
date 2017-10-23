package org.sofi.deadman.messages.validation

sealed trait DomainValidation {
  def error: String
}

final case class InvalidString(field: String) extends DomainValidation {
  val error = s"$field must be a non-empty string"
}

case object InvalidTTL extends DomainValidation {
  val error = "ttl must be >= 1 second"
}

case object InvalidTTW extends DomainValidation {
  val error = "ttw values must be >= 1 second and < ttl"
}

case object InvalidTags extends DomainValidation {
  val error = "tags must be lower case strings without spaces or special characters"
}

case object InvalidTimestamp extends DomainValidation {
  val error = "timestamp must be > 0"
}
