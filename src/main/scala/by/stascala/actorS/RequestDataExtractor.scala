package by.stascala.actorS

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import by.stascala.actorS.CampaignManager.HandleBid
import by.stascala.api.{BidRequest, Impression}
import by.stascala.main.HTTPServer.{BadResponse, Response}

object RequestDataExtractor {

  final case class ExtractRequestData(request: BidRequest, requestHandler: ActorRef[HandleBid], replyTo: ActorRef[Response])

  def apply(): Behaviors.Receive[ExtractRequestData] = Behaviors.receive {
    case (ctx, ExtractRequestData(request, requestHandler, replyTo)) =>
      ctx.log.info(s"Extracting data from $request")

      def deviceGeo: Option[String] =
        for {
          device <- request.device
          geo <- device.geo
          country <- geo.country
        } yield country

      def userGeo: Option[String] =
        for {
          user <- request.user
          geo <- user.geo
          country <- geo.country
        } yield country

      def country: Either[Unit, String] = deviceGeo match {
        case Some(country) => Right(country)
        case None => userGeo match {
          case Some(country) => Right(country)
          case None => Left({
            ctx.log.info("Bad request: Empty country")
            replyTo ! BadResponse
            Behaviors.stopped
          })
        }
      }

      def filteredImpressionList: Either[Unit, List[Impression]] = {
        request.imp match {
          case Some(impressionList) =>
            val filteredList = impressionList.filter(_.bidFloor.isDefined)
            if (filteredList.nonEmpty)
              Right(filteredList)
            else Left({
              ctx.log.info("Bad request: No impressions with defined minimum bid")
              replyTo ! BadResponse
              Behaviors.stopped
            })
          case None => Left({
            ctx.log.info("Bad request: Empty impressions list")
            replyTo ! BadResponse
            Behaviors.stopped
          })
        }
      }

      for {
        country <- country
        impressionList <- filteredImpressionList
      } yield {
        requestHandler ! HandleBid(request.id, request.site.id, country, impressionList, replyTo)
      }
      Behaviors.stopped
  }
}
