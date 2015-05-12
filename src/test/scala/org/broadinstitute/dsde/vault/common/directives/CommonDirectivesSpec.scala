package org.broadinstitute.dsde.vault.common.directives

import org.scalatest.Matchers._
import spray.http.HttpResponse
import spray.http.StatusCodes._
import spray.routing.Directives._
import spray.testkit.RouteTest

trait CommonDirectivesSpec {
  this: RouteTest =>
  lazy val OkResponse = HttpResponse()
  lazy val completeOk = complete(OkResponse)
  lazy val checkOk = check {
    status should be(OK)
    response should be(OkResponse)
  }
  lazy val checkUnhandled = check {
    handled should be(right = false)
  }
}
