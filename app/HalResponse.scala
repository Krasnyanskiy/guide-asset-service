package writer

import play.api.http._
import play.api.libs.json._
import play.api.mvc._

trait HalWriter[T] {
  def write[T](request: RequestHeader) : Writes[T]
}

case class HalResponse[T](model: T, self: Option[Call] = None, additionalLinks: List[(String, String)] = List.empty)
                         (implicit request: RequestHeader, writes: Writes[T]) {


  def toJson[T](implicit request: RequestHeader): JsObject = {
    val selfLink = self map { self =>
      HalResponse.Link("self", self.url, false)
    } getOrElse {
      HalResponse.Link("self", request.uri, false)
    }

    HalResponse.Links(List(selfLink)).asJson ++ Json.toJson(model).as[JsObject]
  }

}

object HalResponse {

  implicit def writableHal[T](implicit codec: Codec, request: RequestHeader): Writeable[HalResponse[T]] =
    Writeable[HalResponse[T]]((result: HalResponse[T]) => codec.encode(result.toJson.toString()),
      Some("application/hal+json"))

  implicit val linksWriter = new Writes[Links] {
    def writes(links: Links): JsObject = Json.obj(
      "_links" -> links.links.foldLeft(Json.obj()) { (json, link) =>
        json + (link.name, Json.obj(
          "href" -> link.href,
          "templated" -> link.templated
        ))
      }
    )
  }

  case class Link(name: String, href: String, templated: Boolean = false)

  case class Links(links: List[Link] = List.empty) {

    def withLinks(link: (String, String, Boolean)*): Links = link.foldLeft(this) { case (newlinks, link) =>
      newlinks.copy(links = newlinks.links :+ (Link.apply _).tupled(link))
    }

    lazy val asJson = linksWriter.writes(this)
  }

}

