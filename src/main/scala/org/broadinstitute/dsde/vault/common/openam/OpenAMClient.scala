package org.broadinstitute.dsde.vault.common.openam

import akka.actor.ActorSystem
import org.broadinstitute.dsde.vault.common.openam.OpenAMResponse._
import spray.client.pipelining._
import spray.http.Uri
import spray.httpx.SprayJsonSupport._

import scala.concurrent.Future

object OpenAMClient {
  implicit val system = ActorSystem()

  import system.dispatcher

  def authenticate(): Future[AuthenticateResponse] =
    authenticate(
      OpenAMConfig.deploymentUri,
      OpenAMConfig.username,
      OpenAMConfig.password,
      OpenAMConfig.realm,
      OpenAMConfig.authIndexType,
      OpenAMConfig.authIndexValue)

  def authenticate(deploymentUri: String, username: String, password: String, realm: Option[String],
                   authIndexType: Option[String], authIndexValue: Option[String]): Future[AuthenticateResponse] = {

    // Add optional information about how we are authenticating
    var queryValues = Seq.empty[(String, String)]
    authIndexType foreach { x => queryValues :+= ("authIndexType" -> x) }
    authIndexValue foreach { x => queryValues :+= ("authIndexValue" -> x) }

    val uri = Uri(s"$deploymentUri/json${realm.getOrElse("")}/authenticate").
      withQuery(queryValues: _*)
    val pipeline =
      addHeader("X-OpenAM-Username", username) ~>
        addHeader("X-OpenAM-Password", password) ~>
        sendReceive ~>
        unmarshal[AuthenticateResponse]
    pipeline(Post(uri))
  }

  def lookupIdFromSession(deploymentUri: String, token: String): Future[IdFromSessionResponse] = {
    val uri = Uri(s"$deploymentUri/json/users").
      withQuery("_action" -> "idFromSession")
    val pipeline =
      addToken(token) ~>
        sendReceive ~>
        unmarshal[IdFromSessionResponse]
    // NOTE: Using the empty map to set the required json content-type header
    pipeline(Post(uri, Map.empty[String, String]))
  }

  /**
   * Retrieves the username and common names (CN) by using the token.
   */
  def lookupUsernameCN(deploymentUri: String, token: String, id: String, realm: Option[String]): Future[UsernameCNResponse] = {
    val uri = Uri(s"$deploymentUri/json${realm.getOrElse("")}/users/$id").
      withQuery("_fields" -> "username,cn")
    val pipeline =
      addToken(token) ~>
        sendReceive ~>
        unmarshal[UsernameCNResponse]
    pipeline(Get(uri))
  }

  private def addToken(token: String) =
    addHeader(OpenAMConfig.tokenCookie, token)
}
