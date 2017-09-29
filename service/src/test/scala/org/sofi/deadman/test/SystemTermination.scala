package org.sofi.deadman.test

import akka.testkit.TestKit
import org.scalatest.{ BeforeAndAfterAll, Suite }

trait SystemTermination extends BeforeAndAfterAll { this: TestKit with Suite ⇒
  override protected def afterAll(): Unit = {
    super.afterAll()
    val _ = system.terminate()
  }
}
