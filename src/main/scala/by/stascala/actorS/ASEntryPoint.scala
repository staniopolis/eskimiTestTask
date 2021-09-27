package by.stascala.actorS

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import by.stascala.api.{BidRequest, Campaigns}
import by.stascala.main.HTTPServer.Response

object ASEntryPoint {
  sealed trait Request

  case class HandleRequest(req: BidRequest, replyTo: ActorRef[Response]) extends Request


  def apply(): Behavior[Request] = {
    Behaviors.setup { context =>
      val campaignManager = context.spawn(CampaignManager(Campaigns.activeCampaigns), "campaign-manager")

      context.log.info("Campaign manager created")
      Behaviors.receiveMessage[Request] {

        case HandleRequest(req, replyTo) =>
          context.log.info("Bid request {} received", req)

          val dataExtractor = context.spawn(RequestDataExtractor.apply(), "data-extractor")
          dataExtractor ! RequestDataExtractor.ExtractRequestData(req, campaignManager, replyTo)
          Behaviors.same
      }
    }
  }
}
