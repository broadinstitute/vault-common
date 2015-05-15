package org.broadinstitute.dsde.vault.common.util

import akka.io.IO
import akka.io.Tcp.CommandFailed
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.Status.Failure
import akka.actor.{ActorSystem, Props}
import spray.can.Http
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

object ServerInitializer {

  /**
    *Manages actorSystem shutdown when Http.bind doesn't succeed in binding the Service to webservicePort
    */
  def startWebServiceActors(props: Props, webserviceInterface: String, webservicePort: Int, timeoutDuration: FiniteDuration, actorSystem: ActorSystem): Unit = {

    val service = actorSystem.actorOf(props, "service")
    implicit val timeout = Timeout(timeoutDuration)

    Await.result(IO(Http)(actorSystem) ? Http.Bind(service, webserviceInterface, webservicePort), timeoutDuration) match {
     case CommandFailed(b: Http.Bind) =>
        actorSystem.log.error(s"Unable to bind to port $webservicePort on interface $webserviceInterface")
        actorSystem.shutdown()
     case Failure(t) =>
        actorSystem.log.error(t, "could not bind to port")
        actorSystem.shutdown()
     case _ =>
    }
  }
}
