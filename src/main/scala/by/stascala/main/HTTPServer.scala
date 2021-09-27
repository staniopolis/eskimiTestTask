package by.stascala.main

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import by.stascala.actorS.ASEntryPoint

import scala.concurrent.duration._
import by.stascala.api.{BidRequest, BidResponse, JsonSupport}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn


object HTTPServer extends SprayJsonSupport with JsonSupport {

  sealed trait Response

  case class ValidResponse(rsp: BidResponse) extends Response

  object BadResponse extends Response

  implicit val system: ActorSystem[ASEntryPoint.Request] = ActorSystem(ASEntryPoint.apply(), "BiddingService")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
  implicit val timeout: Timeout = 3.seconds

  def main(args: Array[String]): Unit = {

    val route: Route = (path("bidService") & post) {
      entity(as[BidRequest]) { request =>
        val response = system.ask(replyTo => ASEntryPoint.HandleRequest(request, replyTo)).mapTo[Response]
        onComplete(response) { res =>
          res.get match {
            case ValidResponse(rsp) => complete(rsp)
            case _ => complete(StatusCodes.NoContent)
          }
        }
      }
    }
    val server = Http().newServerAt("localhost", 8081).bind(route)

    println(s"Server online at http://localhost:8081/)")
    StdIn.readLine(s"Press ENTER to stop...\n")

    server
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
