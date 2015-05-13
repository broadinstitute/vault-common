package org.broadinstitute.dsde.vault.common.directives

import org.broadinstitute.dsde.vault.common.directives.VersioningDirectives._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FreeSpec}
import spray.http._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

class VersioningDirectivesSpec extends FreeSpec with Matchers with ScalatestRouteTest with ScalaFutures with CommonDirectivesSpec with HttpService {

  def actorRefFactory = system

  "VersioningDirectives" - {

    "when using optionalVersion" - {
      "optionalVersion should not get a version when not there" in {
        Get("/hello") ~> {
          pathPrefix("hello") {
            optionalVersion { version =>
              version should be(empty)
              completeOk
            }
          }
        } ~> checkOk
      }

      "optionalVersion should get a version when there" in {
        Get("/hello/v1") ~> {
          pathPrefix("hello") {
            optionalVersion { version =>
              version shouldNot be(empty)
              version.get should be(1)
              completeOk
            }
          }
        } ~> checkOk
      }

      "optionalVersion should not match a non-version" in {
        Get("/hello/2") ~> {
          pathPrefix("hello") {
            optionalVersion { version =>
              path(IntNumber) { i =>
                version should be(empty)
                i should be(2)
                completeOk
              }
            }
          }
        } ~> checkOk
      }

      "optionalVersion should match when followed by a non-version" in {
        Get("/hello/v1/2") ~> {
          pathPrefix("hello") {
            optionalVersion { version =>
              path(IntNumber) { i =>
                version shouldNot be(empty)
                version.get should be(1)
                i should be(2)
                completeOk
              }
            }
          }
        } ~> checkOk
      }
    }

    "when using defaultVersion" - {
      "defaultVersion should return default when version not there" in {
        Get("/hello") ~> {
          pathPrefix("hello") {
            defaultVersion(-1) { version =>
              version should be(-1)
              completeOk
            }
          }
        } ~> checkOk
      }
      "defaultVersion should return version when there" in {
        Get("/hello/v1") ~> {
          pathPrefix("hello") {
            defaultVersion(-1) { version =>
              version should be(1)
              completeOk
            }
          }
        } ~> checkOk
      }

      "defaultVersion should not match a non-version" in {
        Get("/hello/2") ~> {
          pathPrefix("hello") {
            defaultVersion(-1) { version =>
              path(IntNumber) { i =>
                version should be(-1)
                i should be(2)
                completeOk
              }
            }
          }
        } ~> checkOk
      }

      "defaultVersion should match when followed by a non-version" in {
        Get("/hello/v1/2") ~> {
          pathPrefix("hello") {
            defaultVersion(-1) { version =>
              path(IntNumber) { i =>
                version should be(1)
                i should be(2)
                completeOk
              }
            }
          }
        } ~> checkOk
      }
    }

    "when using pathPrefixOptionalVersion" - {
      "pathPrefixOptionalVersion should not get a version when not there" in {
        Get("/hello") ~> {
          pathPrefixOptionalVersion("hello") { version =>
            version should be(empty)
            completeOk
          }
        } ~> checkOk
      }

      "pathPrefixOptionalVersion should get a version when there" in {
        Get("/hello/v1") ~> {
          pathPrefixOptionalVersion("hello") { version =>
            version shouldNot be(empty)
            version.get should be(1)
            completeOk
          }
        } ~> checkOk
      }

      "pathPrefixOptionalVersion should not match a non-version" in {
        Get("/hello/2") ~> {
          pathPrefixOptionalVersion("hello") { version =>
            path(IntNumber) { i =>
              version should be(empty)
              i should be(2)
              completeOk
            }
          }
        } ~> checkOk
      }

      "pathPrefixOptionalVersion should match when followed by a non-version" in {
        Get("/hello/v1/2") ~> {
          pathPrefixOptionalVersion("hello") { version =>
            path(IntNumber) { i =>
              version shouldNot be(empty)
              version.get should be(1)
              i should be(2)
              completeOk
            }
          }
        } ~> checkOk
      }
    }

    "when using pathOptionalVersion" - {
      "pathVersion should not get a version when not there" in {
        Get("/hello") ~> {
          pathOptionalVersion("hello") { version =>
            version should be(empty)
            completeOk
          }
        } ~> checkOk
      }

      "pathOptionalVersion should get a version when there" in {
        Get("/hello/v1") ~> {
          pathOptionalVersion("hello") { version =>
            version shouldNot be(empty)
            version.get should be(1)
            completeOk
          }
        } ~> checkOk
      }

      "pathOptionalVersion should not match a non-version" in {
        Get("/hello/2") ~> {
          pathOptionalVersion("hello") { version =>
            path(IntNumber) { i =>
              fail()
            }
          }
        } ~> checkUnhandled
      }

      "pathOptionalVersion should match when followed by a non-version" in {
        Get("/hello/v1/2") ~> {
          pathOptionalVersion("hello") { version =>
            path(IntNumber) { i =>
              fail()
            }
          }
        } ~> checkUnhandled
      }
    }

    "when using pathOptionalVersion with a suffix" - {
      "pathOptionalVersion matching for a suffix should not match a non-version when no suffix" in {
        Get("/hello") ~> {
          pathOptionalVersion("hello", IntNumber) { (version, i) =>
            fail()
          }
        } ~> checkUnhandled
      }

      "pathOptionalVersion matching for a suffix should not match a version when no suffix" in {
        Get("/hello/v1") ~> {
          pathOptionalVersion("hello", IntNumber) { (version, i) =>
            fail()
          }
        } ~> checkUnhandled
      }

      "pathOptionalVersion with a suffix should not get a version when not there" in {
        Get("/hello/2") ~> {
          pathOptionalVersion("hello", IntNumber) { (version, i) =>
            version should be(empty)
            i should be(2)
            completeOk
          }
        } ~> checkOk
      }

      "pathOptionalVersion with a suffix should get a version when there" in {
        Get("/hello/v1/2") ~> {
          pathOptionalVersion("hello", IntNumber) { (version, i) =>
            version shouldNot be(empty)
            version.get should be(1)
            i should be(2)
            completeOk
          }
        } ~> checkOk
      }

      "pathOptionalVersion with multiple suffixes should not get a version when not there" in {
        Get("/hello/2/a1") ~> {
          pathOptionalVersion("hello", IntNumber / Segment) { (version, i, str) =>
            version should be(empty)
            i should be(2)
            str should be("a1")
            completeOk
          }
        } ~> checkOk
      }

      "pathOptionalVersion with multiple suffixes should get a version when there" in {
        Get("/hello/v1/2/a1") ~> {
          pathOptionalVersion("hello", IntNumber / Segment ) { (version, i, str) =>
            version shouldNot be(empty)
            version.get should be(1)
            i should be(2)
            str should be("a1")
            completeOk
          }
        } ~> checkOk
      }
    }

    "when using pathPrefixVersion" - {
      "pathPrefixVersion should not get a version when not there" in {
        Get("/hello") ~> {
          pathPrefixVersion("hello", -1) { version =>
            version should be(-1)
            completeOk
          }
        } ~> checkOk
      }

      "pathPrefixVersion should get a version when there" in {
        Get("/hello/v1") ~> {
          pathPrefixVersion("hello", -1) { version =>
            version should be(1)
            completeOk
          }
        } ~> checkOk
      }

      "pathPrefixVersion should not match a non-version" in {
        Get("/hello/2") ~> {
          pathPrefixVersion("hello", -1) { version =>
            path(IntNumber) { i =>
              version should be(-1)
              i should be(2)
              completeOk
            }
          }
        } ~> checkOk
      }

      "pathPrefixVersion should match when followed by a non-version" in {
        Get("/hello/v1/2") ~> {
          pathPrefixVersion("hello", -1) { version =>
            path(IntNumber) { i =>
              version should be(1)
              i should be(2)
              completeOk
            }
          }
        } ~> checkOk
      }
    }

    "when using pathVersion" - {
      "pathVersion should not get a version when not there" in {
        Get("/hello") ~> {
          pathVersion("hello", -1) { version =>
            version should be(-1)
            completeOk
          }
        } ~> checkOk
      }

      "pathVersion should get a version when there" in {
        Get("/hello/v1") ~> {
          pathVersion("hello", -1) { version =>
            version should be(1)
            completeOk
          }
        } ~> checkOk
      }

      "pathVersion should not match a non-version" in {
        Get("/hello/2") ~> {
          pathVersion("hello", -1) { version =>
            fail()
          }
        } ~> checkUnhandled
      }

      "pathVersion should not match when followed by a non-version" in {
        Get("/hello/v1/2") ~> {
          pathVersion("hello", -1) { version =>
            fail()
          }
        } ~> checkUnhandled
      }
    }

    "when using pathVersion with a suffix" - {
      "pathVersion matching for a suffix should not match a non-version when no suffix" in {
        Get("/hello") ~> {
          pathVersion("hello", -1, IntNumber) { (version, i) =>
            fail()
          }
        } ~> checkUnhandled
      }

      "pathVersion matching for a suffix should not match a version when no suffix" in {
        Get("/hello/v1") ~> {
          pathVersion("hello", -1, IntNumber) { (version, i) =>
            fail()
          }
        } ~> checkUnhandled
      }

      "pathVersion with a suffix should not get a version when not there" in {
        Get("/hello/2") ~> {
          pathVersion("hello", -1, IntNumber) { (version, i) =>
            version should be(-1)
            i should be(2)
            completeOk
          }
        } ~> checkOk
      }

      "pathVersion with a suffix should get a version when there" in {
        Get("/hello/v1/2") ~> {
          pathVersion("hello", -1, IntNumber) { (version, i) =>
            version should be(1)
            i should be(2)
            completeOk
          }
        } ~> checkOk
      }

      "pathVersion with multiple suffixes should not get a version when not there" in {
        Get("/hello/2/a1") ~> {
          pathVersion("hello", -1, IntNumber / Segment) { (version, i, str) =>
            version should be(-1)
            i should be(2)
            str should be("a1")
            completeOk
          }
        } ~> checkOk
      }

      "pathVersion with multiple suffixes should get a version when there" in {
        Get("/hello/v1/2/a1") ~> {
          pathVersion("hello", -1, IntNumber / Segment ) { (version, i, str) =>
            version should be(1)
            i should be(2)
            str should be("a1")
            completeOk
          }
        } ~> checkOk
      }
    }
  }
}
