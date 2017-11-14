package org.sofi.deadman.client

case class Query(id: String, queryType: Query.QueryType, queryKey: Query.QueryKey = Query.NoKey)

object Query {

  implicit class QueryOpts(val query: Query) extends AnyVal {
    def uri = s"${query.queryType.value}/${query.id}/${query.queryKey.value}"
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

  sealed trait QueryKey {
    def value: String
  }

  case object NoKey extends QueryKey {
    val value = ""
  }

  case object Expirations extends QueryKey {
    val value = "expirations"
  }

  case object Warnings extends QueryKey {
    val value = "warnings"
  }
}
