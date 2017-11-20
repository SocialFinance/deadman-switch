package controllers

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._

/**
 * Integration test to run through event source.
 */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {
  "The home controller" should {
    "return OK through route" in {
      val request = FakeRequest(method = GET, path = "/deadman/")
      route(app, request) match {
        case Some(future) ⇒
          whenReady(future) { result ⇒
            result.header.status mustEqual OK
          }
        case None ⇒
          fail
      }
    }
  }
}
