package org.broadinstitute.dsde.vault.common.openam

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import spray.testkit.ScalatestRouteTest

import scala.concurrent.duration._

class OpenAMSessionSpec extends FreeSpec with Matchers with ScalatestRouteTest with ScalaFutures {
  "OpenAMSession" - {
    "when accessing the OpenAM session" - {
      "the openAMSession should be valid" in {
        val openAMSession = OpenAMSession(()).futureValue(timeout(scaled(OpenAMConfig.timeoutSeconds.seconds)), interval(scaled(0.5.seconds)))
        openAMSession.cookies.size should be(1)
        openAMSession.cookies.head.name should be(OpenAMConfig.tokenCookie)
      }
    }
  }
}
