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
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
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
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
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
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
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
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }
    }

    "when using pathPrefixVersion" - {
      "pathPrefixVersion should not get a version when not there" in {
        Get("/hello") ~> {
          pathPrefixVersion("hello") { version =>
            version should be(empty)
            completeOk
          }
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }

      "pathPrefixVersion should get a version when there" in {
        Get("/hello/v1") ~> {
          pathPrefixVersion("hello") { version =>
            version shouldNot be(empty)
            version.get should be(1)
            completeOk
          }
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }

      "pathPrefixVersion should not match a non-version" in {
        Get("/hello/2") ~> {
          pathPrefixVersion("hello") { version =>
            path(IntNumber) { i =>
              version should be(empty)
              i should be(2)
              completeOk
            }
          }
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }

      "pathPrefixVersion should match when followed by a non-version" in {
        Get("/hello/v1/2") ~> {
          pathPrefixVersion("hello") { version =>
            path(IntNumber) { i =>
              version shouldNot be(empty)
              version.get should be(1)
              i should be(2)
              completeOk
            }
          }
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }
    }

    "when using pathVersion" - {
      "pathVersion should not get a version when not there" in {
        Get("/hello") ~> {
          pathVersion("hello") { version =>
            version should be(empty)
            completeOk
          }
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }

      "pathVersion should get a version when there" in {
        Get("/hello/v1") ~> {
          pathVersion("hello") { version =>
            version shouldNot be(empty)
            version.get should be(1)
            completeOk
          }
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }

      "pathVersion should not match a non-version" in {
        Get("/hello/2") ~> {
          pathVersion("hello") { version =>
            path(IntNumber) { i =>
              fail()
            }
          }
        } ~> check {
          handled should be(right = false)
        }
      }

      "pathVersion should match not match when followed by a non-version" in {
        Get("/hello/v1/2") ~> {
          pathVersion("hello") { version =>
            path(IntNumber) { i =>
              fail()
            }
          }
        } ~> check {
          handled should be(right = false)
        }
      }
    }


    "when using pathVersion with a suffix" - {
      "pathVersion matching for a suffix should not match a non-version when no suffix" in {
        Get("/hello") ~> {
          pathVersion("hello", IntNumber) { (version, i) =>
            fail()
          }
        } ~> check {
          handled should be(right = false)
        }
      }

      "pathVersion matching for a suffix should not match a version when no suffix" in {
        Get("/hello/v1") ~> {
          pathVersion("hello", IntNumber) { (version, i) =>
            fail()
          }
        } ~> check {
          handled should be(right = false)
        }
      }

      "pathVersion with a suffix should not get a version when not there" in {
        Get("/hello/2") ~> {
          pathVersion("hello", IntNumber) { (version, i) =>
            version should be(empty)
            i should be(2)
            completeOk
          }
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }

      "pathVersion with a suffix should get a version when there" in {
        Get("/hello/v1/2") ~> {
          pathVersion("hello", IntNumber) { (version, i) =>
            version shouldNot be(empty)
            version.get should be(1)
            i should be(2)
            completeOk
          }
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }

      "pathVersion with multiple suffixes should not get a version when not there" in {
        Get("/hello/2/a1") ~> {
          pathVersion("hello", IntNumber / Segment) { (version, i, str) =>
            version should be(empty)
            i should be(2)
            str should be("a1")
            completeOk
          }
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }

      "pathVersion with multiple suffixes should get a version when there" in {
        Get("/hello/v1/2/a1") ~> {
          pathVersion("hello", IntNumber / Segment ) { (version, i, str) =>
            version shouldNot be(empty)
            version.get should be(1)
            i should be(2)
            str should be("a1")
            completeOk
          }
        } ~> check {
          status should be(OK)
          response should be(OkResponse)
        }
      }
    }
  }
}
