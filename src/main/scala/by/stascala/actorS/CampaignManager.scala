package by.stascala.actorS

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import by.stascala.api.{Banner, BidResponse, Campaign, Impression}
import by.stascala.main.HTTPServer.{BadResponse, Response, ValidResponse}

import java.util.UUID
import scala.util.Random

object CampaignManager {

  final case class HandleBid(id: String, siteId: String, country: String, impressions: List[Impression], replyTo: ActorRef[Response])

  def apply(campaigns: Seq[Campaign]): Behaviors.Receive[HandleBid] = Behaviors.receive {
    case (ctx, req: HandleBid) =>
      val result = handleRequest(req, campaigns)
      result match {
        case (Some(imp), Some(camp), Some(banner)) =>
          // Building response
          val response = ValidResponse(BidResponse(id = UUID.randomUUID().toString,
            bidRequestId = req.id,
            price = imp.bidFloor.get,
            adid = Option(camp.id.toString),
            banner = Option(banner)
          ))
          ctx.log.info(s"A matching campaign found: $response")
          req.replyTo ! response
        case _ =>
          ctx.log.info(s"No matching campaign found")
          req.replyTo ! BadResponse
      }
      Behaviors.same
  }

  def handleRequest(request: HandleBid, campaigns: Seq[Campaign]): (Option[Impression], Option[Campaign], Option[Banner]) = {

    // Filter existing Campaigns by the requested country
    val campaignsFilteredByCountry = campaigns.filter(_.country == request.country)

    // Filter Campaigns by the requested targets (in this case Site Id)
    lazy val campaignsFilteredByTargeting = campaignsFilteredByCountry.filter(_.targeting.targetedSiteIds.contains(request.siteId))

    //Mapping each requested Impression to a matching list of Campaigns by minimum bid
    lazy val impressionsMapedToCampaignsByBidFloor =
      (for {
        impression <- request.impressions
        campaign <- campaignsFilteredByTargeting
        if impression.bidFloor.get <= campaign.bid
      } yield (impression, campaign))
        .groupBy(_._1)
        .map(m => m._1 -> m._2.map(_._2))

    // Filter each Campaigns list of Banners by matching to the requested
    lazy val campaignsFilteredByValidBanners =
      impressionsMapedToCampaignsByBidFloor.map(kv => kv._1 -> kv._2.map(campaigns =>
        Campaign(campaigns.id, campaigns.country, campaigns.targeting, campaigns.banners.filter(bannerFilter(kv._1, _)), campaigns.bid)))

    // Filter out Campaigns with empty list of Banners
    lazy val campaignsFilteredWithNonEmptyBanners =
      campaignsFilteredByValidBanners.filter(kv => (kv._1 -> kv._2.filter(_.banners.nonEmpty))._2.nonEmpty)

    // Checking variables and choosing random: Impression, Campaign and Banner
    if (campaignsFilteredByCountry.nonEmpty &&
      campaignsFilteredByTargeting.nonEmpty &&
      impressionsMapedToCampaignsByBidFloor.nonEmpty &&
      campaignsFilteredWithNonEmptyBanners.nonEmpty) {

      val impressions = campaignsFilteredWithNonEmptyBanners.keySet.toList
      val randomImp = impressions(Random.nextInt(impressions.length))
      val campaigns = campaignsFilteredWithNonEmptyBanners(randomImp)
      val randomCamp = campaigns(Random.nextInt(campaigns.length))
      val randomBanner = randomCamp.banners(Random.nextInt(randomCamp.banners.length))

      (Option(randomImp), Option(randomCamp), Option(randomBanner))
    } else (None, None, None)
  }

  // Banner filter method
  def bannerFilter(imp: Impression, banner: Banner): Boolean =
    imp match {
      // Fixed height and width
      case Impression(_, _, _, Some(w), _, _, Some(h), _) =>
        banner.width == w && banner.height == h

      //Ranged width and direct height
      case Impression(_, Some(wmin), Some(wmax), _, _, _, Some(h), _) =>
        banner.width >= wmin && banner.width <= wmax && banner.height == h
      case Impression(_, Some(wmin), _, _, _, _, Some(h), _) =>
        banner.width >= wmin && banner.height == h
      case Impression(_, _, Some(wmax), _, _, _, Some(h), _) =>
        banner.width <= wmax && banner.height == h

      // Ranged height and direct width
      case Impression(_, _, _, Some(w), Some(hmin), Some(hmax), _, _) =>
        banner.width == w && banner.height <= hmax && banner.height >= hmin
      case Impression(_, _, _, Some(w), Some(hmin), _, _, _) =>
        banner.width == w && banner.height >= hmin
      case Impression(_, _, _, Some(w), _, Some(hmax), _, _) =>
        banner.width == w && banner.height <= hmax

      // Ranged height and width
      case Impression(_, Some(wmin), Some(wmax), _, Some(hmin), Some(hmax), _, _) =>
        banner.width >= wmin && banner.width <= wmax && banner.height <= hmax && banner.height >= hmin

      case Impression(_, Some(wmin), Some(wmax), _, _, Some(hmax), _, _) =>
        banner.width >= wmin && banner.width <= wmax && banner.height <= hmax
      case Impression(_, Some(wmin), Some(wmax), _, Some(hmin), _, _, _) =>
        banner.width >= wmin && banner.width <= wmax && banner.height >= hmin

      case Impression(_, _, Some(wmax), _, Some(hmin), Some(hmax), _, _) =>
        banner.width <= wmax && banner.height <= hmax && banner.height >= hmin
      case Impression(_, Some(wmin), _, _, Some(hmin), Some(hmax), _, _) =>
        banner.width >= wmin && banner.height <= hmax && banner.height >= hmin


      case Impression(_, _, Some(wmax), _, _, Some(hmax), _, _) =>
        banner.width <= wmax && banner.height <= hmax
      case Impression(_, Some(wmin), _, _, _, Some(hmax), _, _) =>
        banner.width >= wmin && banner.height <= hmax
      case Impression(_, _, Some(wmax), _, Some(hmin), _, _, _) =>
        banner.width <= wmax && banner.height >= hmin
      case Impression(_, Some(wmin), _, _, Some(hmin), _, _, _) =>
        banner.width >= wmin && banner.height >= hmin
    }

}
