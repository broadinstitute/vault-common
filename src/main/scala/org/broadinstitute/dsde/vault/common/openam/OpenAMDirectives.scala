package org.broadinstitute.dsde.vault.common.openam

import org.broadinstitute.dsde.vault.common.util.ImplicitMagnet
import org.slf4j.LoggerFactory
import spray.routing.{Directive0, Directive1}
import spray.routing.Directives._

import scala.concurrent.ExecutionContext

object OpenAMDirectives {
  val tokenFromCookie: Directive1[String] = cookie(OpenAMConfig.tokenCookie) map {
    // return the content of the cookie
    _.content
  }

  val tokenFromOptionalCookie: Directive1[Option[String]] = optionalCookie(OpenAMConfig.tokenCookie) map {
    // when there is a cookie
    _ map {
      // return the content of the cookie
      _.content
    }
  }

  def commonNameFromCookie(magnet: ImplicitMagnet[ExecutionContext]): Directive1[String] = {
    implicit val ec = magnet.value
    tokenFromCookie flatMap commonNameFromToken
  }

  def commonNameFromOptionalCookie(magnet: ImplicitMagnet[ExecutionContext]): Directive1[Option[String]] = {
    implicit val ec = magnet.value
    tokenFromOptionalCookie flatMap {
      case Some(token) => commonNameFromToken(token).map(Option(_))
      case None => provide(None)
    }
  }

  // Internet says hit slf4j directly if outside an actor...
  // https://groups.google.com/forum/#!topic/akka-user/_bIiPKoGJXY
  lazy val logOpenAMRequestLogger = LoggerFactory.getLogger("org.broadinstitute.dsde.vault.common.openam.OpenAMRequest")

  // Partially based off DebuggingDirectives.logRequest, sans magnets, with requestInstance instead of mapRequest
  // With an execution context in scope, one may use "logOpenAMRequest() {...}", but not "logOpenAMRequest {...}".
  def logOpenAMRequest(magnet: ImplicitMagnet[ExecutionContext]): Directive0 = {
    commonNameFromCookie(magnet) flatMap { commonName =>
      requestInstance flatMap { request =>
        // Quoting based off ELF string format: http://en.wikipedia.org/wiki/Extended_Log_Format
        val commonNameQuoted = """"%s"""".format(commonName.replaceAll("\"", "\"\""))
        val method = request.method.name
        val uri = request.uri
        var message = s"$commonNameQuoted $method $uri"
        if (logOpenAMRequestLogger.isDebugEnabled) {
          if (request.entity.nonEmpty) {
            message += "%n".format() + request.entity.asString
          }
        }
        logOpenAMRequestLogger.info(message)
        pass
      }
    }
  }

  private def commonNameFromToken(token: String)(implicit ec: ExecutionContext): Directive1[String] = {
    val userNameFuture = for {
      id <- OpenAMClient.lookupIdFromSession(OpenAMConfig.deploymentUri, token)
      usernameCN <- OpenAMClient.lookupUsernameCN(OpenAMConfig.deploymentUri, token, id.id, id.realm)
    } yield usernameCN.cn.head
    onSuccess(userNameFuture)
  }
}
