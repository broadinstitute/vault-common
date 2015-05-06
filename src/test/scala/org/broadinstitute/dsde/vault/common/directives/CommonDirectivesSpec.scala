package org.broadinstitute.dsde.vault.common.directives

import spray.http.HttpResponse
import spray.routing.Directives._

trait CommonDirectivesSpec {
  lazy val OkResponse = HttpResponse()
  lazy val completeOk = complete(OkResponse)
}
