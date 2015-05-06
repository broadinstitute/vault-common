package org.broadinstitute.dsde.vault.common.openam

import org.broadinstitute.dsde.vault.common.openam.OpenAMResponse.{AuthenticateResponse, IdFromSessionResponse}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import spray.can.Http.ConnectionAttemptFailedException
import spray.httpx.UnsuccessfulResponseException

import scala.concurrent.duration._

class OpenAMClientSpec extends FreeSpec with Matchers with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(OpenAMConfig.timeoutSeconds.seconds, scaled(0.5.seconds))

  "OpenAMClient" - {
    "when accessing OpenAM" - {

      "The basic OpenAM properties should be set" in {
        OpenAMConfig.deploymentUri shouldNot be(empty)
        OpenAMConfig.deploymentUri shouldNot be("replace_with_openam_deployment_uri")
        OpenAMConfig.deploymentUri shouldNot endWith("/")
        OpenAMConfig.username shouldNot be(empty)
        OpenAMConfig.username shouldNot be("replace_with_openam_username")
        OpenAMConfig.password shouldNot be(empty)
        OpenAMConfig.password shouldNot be("replace_with_openam_password")

        // Please start the sub-realms with a slash! If this requirement changes, delete this test.
        OpenAMConfig.realm foreach { realm =>
          realm should startWith("/")
        }
      }

      // ===== Authentication =====

      var auth: Option[AuthenticateResponse] = None

      "Authentication should return successfully" in {
        auth = Option(OpenAMClient.authenticate(OpenAMConfig.deploymentUri,
          OpenAMConfig.username, OpenAMConfig.password,
          OpenAMConfig.realm, OpenAMConfig.authIndexType, OpenAMConfig.authIndexValue).futureValue)
        auth shouldNot be(empty)
        auth.get.tokenId shouldNot be(empty)
        auth.get.successUrl shouldNot be(empty)
      }

      "Authentication with a bad host name should fail" in {
        intercept[ConnectionAttemptFailedException] {
          throw OpenAMClient.authenticate("https://bad_host", OpenAMConfig.username, OpenAMConfig.password,
            OpenAMConfig.realm, OpenAMConfig.authIndexType, OpenAMConfig.authIndexValue).failed.futureValue
        }
      }

      "Authentication with a bad username should fail" in {
        intercept[UnsuccessfulResponseException] {
          throw OpenAMClient.authenticate(OpenAMConfig.deploymentUri, "bad_username", OpenAMConfig.password,
            OpenAMConfig.realm, OpenAMConfig.authIndexType, OpenAMConfig.authIndexValue).failed.futureValue
        }
      }

      "Authentication with a bad password should fail" in {
        intercept[UnsuccessfulResponseException] {
          throw OpenAMClient.authenticate(OpenAMConfig.deploymentUri, OpenAMConfig.username, "bad_password",
            OpenAMConfig.realm, OpenAMConfig.authIndexType, OpenAMConfig.authIndexValue).failed.futureValue
        }
      }

      // ===== ID Lookup =====

      var id: Option[IdFromSessionResponse] = None

      "ID lookup should return successfully" in {
        assume(auth.isDefined)
        id = Option(OpenAMClient.lookupIdFromSession(OpenAMConfig.deploymentUri, auth.get.tokenId).futureValue)
        id shouldNot be(empty)
        id.get.id shouldNot be(empty)
      }

      "ID lookup of a bad token should fail" in {
        intercept[UnsuccessfulResponseException] {
          throw OpenAMClient.lookupIdFromSession(OpenAMConfig.deploymentUri, "bad_token").failed.futureValue
        }
      }

      // ===== Username Lookup =====

      "Username lookup should return successfully" in {
        assume(id.isDefined)
        val usernameCN = OpenAMClient.lookupUsernameCN(OpenAMConfig.deploymentUri,
          auth.get.tokenId, id.get.id, id.get.realm).futureValue
        usernameCN.username shouldNot be(empty)
        usernameCN.cn shouldNot be(empty)
        usernameCN.cn.head shouldNot be(empty)
      }

      "Username lookup of a bad token should fail" in {
        assume(id.isDefined)
        intercept[UnsuccessfulResponseException] {
          throw OpenAMClient.lookupUsernameCN(OpenAMConfig.deploymentUri, "bad_token", id.get.id, id.get.realm).failed.futureValue
        }
      }

      "Username lookup of a bad id should fail" in {
        assume(id.isDefined)
        intercept[UnsuccessfulResponseException] {
          throw OpenAMClient.lookupUsernameCN(OpenAMConfig.deploymentUri, auth.get.tokenId, "bad_id", id.get.realm).failed.futureValue
        }
      }

      "Username lookup of a bad realm should fail" in {
        assume(id.isDefined)
        intercept[UnsuccessfulResponseException] {
          throw OpenAMClient.lookupUsernameCN(OpenAMConfig.deploymentUri, auth.get.tokenId, id.get.id, Option("/bad_realm")).failed.futureValue
        }
      }
    }
  }
}
