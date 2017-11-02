package org.sofi.deadman.messages

import cats.implicits._
import org.sofi.deadman.messages.command._
import scala.concurrent.duration._
import scala.util.Try

package object validation {

  type ValidationResult[A] = cats.data.ValidatedNel[DomainValidation, A]

  // String field must be non-empty
  private def validateString(field: String, value: String) =
    if (Try(value.trim).getOrElse("").nonEmpty) value.validNel else InvalidString(field).invalidNel

  // Key must be non-empty strings with only upper-case chars, lower-case chars and numbers
  private def validateKey(key: String) =
    if (Try(key.trim).getOrElse("").matches("^[a-zA-Z0-9]+$")) key.validNel else InvalidKey.invalidNel

  // Parse a duration string
  private def parseDuration(d: String) = Try(Duration(d)).getOrElse(0.seconds)

  // TTL must be longer than one second
  private def validateTTL(ttls: String) = {
    val ttl = parseDuration(ttls)
    if (ttl >= 1.second) ttl.toMillis.validNel else InvalidTTL.invalidNel
  }

  // All TTW values must be longer than one second and less than the TTL value
  private def validateTTW(ttws: Seq[String], ttls: String) = {
    val ttw = ttws.map(parseDuration)
    val ttl = parseDuration(ttls)
    if (ttw.forall(v ⇒ v >= 1.second && v < ttl)) ttw.map(_.toMillis).validNel else InvalidTTW.invalidNel
  }

  // Tags must be lower-case strings
  private def validateTags(tags: Seq[String]) =
    if (tags.forall(_.matches("^[a-z]+$"))) tags.validNel else InvalidTags.invalidNel

  // Timestamp must be greater than the epoch
  private def validateTimestamp(ts: Option[Long]) =
    if (ts.getOrElse(System.currentTimeMillis()) > 0) ts.validNel else InvalidTimestamp.invalidNel

  // Validate data fields and map them to a ScheduleTask command
  def validate(key: String, agg: String, ent: String, ttl: String, ttw: Seq[String], tags: Seq[String], ts: Option[Long]): ValidationResult[ScheduleTask] = (
    validateKey(key),
    validateString("aggregate", agg),
    validateString("entity", ent),
    validateTTL(ttl),
    validateTTW(ttw, ttl),
    validateTags(tags),
    validateTimestamp(ts)
  ).mapN(ScheduleTask.apply)

  // Validate data fields and map them to a CompleteTask command
  def validateCompletion(key: String, agg: String, ent: String): ValidationResult[CompleteTask] = (
    validateKey(key),
    validateString("aggregate", agg),
    validateString("entity", ent)
  ).mapN(CompleteTask.apply)
}
