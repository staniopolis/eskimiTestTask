package by.stascala.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  //Request
  implicit val impressionFormat: RootJsonFormat[Impression] = jsonFormat8(Impression)
  implicit val siteFormat: RootJsonFormat[Site] = jsonFormat2(Site)
  implicit val geoFormat: RootJsonFormat[Geo] = jsonFormat1(Geo)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)
  implicit val deviceFormat: RootJsonFormat[Device] = jsonFormat2(Device)
  implicit val bidRequestFormat: RootJsonFormat[BidRequest] = jsonFormat5(BidRequest)
  //Response
  implicit val bannerFormat: RootJsonFormat[Banner] = jsonFormat4(Banner)
  implicit val bidResponseFormat: RootJsonFormat[BidResponse] = jsonFormat5(BidResponse)
}
