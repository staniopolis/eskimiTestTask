package by.stascala.api

case class Campaign(id: Int, country: String, targeting: Targeting, banners: List[Banner], bid: Double)

case class Targeting(targetedSiteIds: List[String])

case class Banner(id: Int, src: String, width: Int, height: Int)


case class Impression(id: String,
                      wmin: Option[Int], wmax: Option[Int], w: Option[Int],
                      hmin: Option[Int], hmax: Option[Int], h: Option[Int],
                      bidFloor: Option[Double])

case class Site(id: String, domain: String)

case class User(id: String, geo: Option[Geo])

case class Device(id: String, geo: Option[Geo])

case class Geo(country: Option[String])

case class BidRequest(id: String, imp: Option[List[Impression]],
                      site: Site, user: Option[User], device: Option[Device])

case class BidResponse(id: String, bidRequestId: String,
                       price: Double, adid: Option[String],
                       banner: Option[Banner])

object Campaigns{
  val activeCampaigns = Seq(
    Campaign(
      id = 1,
      country = "LT",
      targeting = Targeting(
        targetedSiteIds = List("0006a522ce0f4bbbbaa6b3c38cafaa0f")
      ),
      banners = List(
        Banner(
          id = 1,
          src = "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
          width = 300,
          height = 250
        )
      ),
      bid = 5d
    )
  )
}



