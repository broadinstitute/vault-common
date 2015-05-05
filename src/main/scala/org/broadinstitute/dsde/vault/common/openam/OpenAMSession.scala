package org.broadinstitute.dsde.vault.common.openam

import org.broadinstitute.dsde.vault.common.util.ImplicitMagnet
import spray.http.HttpCookie
import spray.http.HttpHeaders.Cookie

import scala.concurrent.{Future, ExecutionContext}

object OpenAMSession {
  def apply(magnet: ImplicitMagnet[ExecutionContext]): Future[Cookie] = {
    implicit val ec = magnet.value
    OpenAMClient.authenticate().map(resp =>
      Cookie(Seq(HttpCookie(OpenAMConfig.tokenCookie, resp.tokenId))))
  }
}
