package org.sofi.deadman.client

import org.scalatest._

final class QueryTest extends FlatSpec with Matchers {

  import Query._

  final val id = "1"
  final val window = Some("2018-11")

  "Valid aggregate queries" should "return None" in {
    Query(id, Aggregate, Active).validate shouldBe None
    Query(id, Aggregate, Expired).validate shouldBe None
    Query(id, Aggregate, Warning).validate shouldBe None
  }

  "Valid entity queries" should "return None" in {
    Query(id, Entity, Active).validate shouldBe None
    Query(id, Entity, Expired).validate shouldBe None
    Query(id, Entity, Warning).validate shouldBe None
  }

  "Valid key queries" should "return None" in {
    Query(id, Key, Active).validate shouldBe None
    Query(id, Key, Expired, window).validate shouldBe None
  }

  "A Valid tag queries" should "return None" in {
    // Tags can only be queried after expiration
    Query(id, Tag, Expired, window).validate shouldBe None
  }

  "An invalid aggregate query" should "return an error" in {
    // Aggregate expirations aren't grouped by time window
    Query(id, Aggregate, Expired, window).validate match {
      case Some(err) ⇒ err.getMessage shouldBe "Illegal Query: Aggregate+Expired+Some(2018-11)"
      case _ ⇒ fail("Query test failed")
    }
  }

  "An invalid tag query" should "return an error" in {
    Query(id, Tag, Warning).validate match {
      case Some(err) ⇒ err.getMessage shouldBe "Illegal Query: Tag+Warning+None"
      case _ ⇒ fail("Query test failed")
    }
  }

  "An invalid key query" should "return an error" in {
    Query(id, Key, Warning).validate match {
      case Some(err) ⇒ err.getMessage shouldBe "Illegal Query: Key+Warning+None"
      case _ ⇒ fail("Query test failed")
    }
  }
}
