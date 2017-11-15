package org.sofi.deadman.client

case class Query(
  id: String,
  typ: Query.QueryType,
  status: Query.QueryStatus = Query.Active,
  window: Option[String] = None
)

object Query {

  implicit class QueryOpts(val q: Query) extends AnyVal {
    def uri: String = {
      val base = s"${q.typ.value}/${q.id}/${q.status.value}"
      q.window.map(w ⇒ s"$base/$w").getOrElse(base)
    }
    def validate: Option[Throwable] = (q.typ, q.status, q.window) match {
      case (Aggregate, Active, None) ⇒ None
      case (Entity, Active, None) ⇒ None
      case (Key, Active, None) ⇒ None
      case (Aggregate, Expired, None) ⇒ None
      case (Entity, Expired, None) ⇒ None
      case (Key, Expired, Some(_)) ⇒ None
      case (Tag, Expired, Some(_)) ⇒ None
      case (Aggregate, Warning, None) ⇒ None
      case (Entity, Warning, None) ⇒ None
      case _ ⇒
        Some(new IllegalStateException(s"Illegal Query: ${q.typ}+${q.status}+${q.window}"))
    }
    def exec[M[_]]()(implicit client: Client[M]) = client.tasks(q)
  }

  sealed trait QueryType {
    def value: String
  }

  case object Aggregate extends QueryType {
    val value = "aggregate"
  }

  case object Entity extends QueryType {
    val value = "entity"
  }

  case object Key extends QueryType {
    val value = "key"
  }

  case object Tag extends QueryType {
    val value = "tag"
  }

  sealed trait QueryStatus {
    def value: String
  }

  case object Active extends QueryStatus {
    val value = ""
  }

  case object Expired extends QueryStatus {
    val value = "expirations"
  }

  case object Warning extends QueryStatus {
    val value = "warnings"
  }
}
