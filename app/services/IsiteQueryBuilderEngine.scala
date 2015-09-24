package services

import play.api.libs.json.{JsValue, JsObject, Json}
import java.net.URLEncoder
import scala.language.implicitConversions

object IsiteQueryBuilderEngine {

  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  private val guideNamespace = "https://production.bbc.co.uk/isite2/project/guides/guide"
  private val project = "guides"
  private val defaultQuery = Json.arr("ns:id", "contains", "*z*")
  private val sortByMostRecent =
    Json.arr(
      Json.obj(
        "elementPath" -> "meta:modifiedDateTime",
        "direction" -> "desc"
      )
    )

  /**
   *
   * @param baseUrl baseUrl is passed from the AssetsApplication
   * @param query query can be changed as in 'category=football' OR 'nation=eng' OR 'department=knowledge_learning'
   * @param page page number
   * @param pageSize pageSize defines items contained in a page
   */
  case class IsiteQueryBuilder(
                                baseUrl: String,
                                query: JsValue,
                                page: Int,
                                pageSize: Int) {

    def withPageFilter(page: Pagination) = this.copy(page = page.page, pageSize = page.pageSize)

    def withQuery(q: String) = {
      val updatedQuery = q match {
        case "sort=most_recent" => defaultQuery
        case r"(.*)${key}\=(.*)${value}" =>
          Json.obj(
            "and" -> Json.arr(
              defaultQuery,
              Json.arr("ns:"+ key, "=", value)
            )
          )

        case _ => defaultQuery
      }

      this.copy(query = updatedQuery)
    }
  }

  object IsiteQueryBuilder {

    private def isiteQueryBuilder2Json(builder: IsiteQueryBuilder): JsObject = {
      Json.obj(
        "namespaces" -> Json.obj(
          "ns" -> guideNamespace
        ),
        "project" -> project,
        "depth" -> 0,
        "query" -> builder.query,
        "sort" -> sortByMostRecent,
        "page" -> builder.page,
        "pageSize" -> builder.pageSize
      )
    }

    def apply(baseUrl: String) = new IsiteQueryBuilder(baseUrl, defaultQuery, 1, 10)

    implicit def buildQueryString(ib: IsiteQueryBuilder): String = {
      val json = isiteQueryBuilder2Json(ib)
      val encodedQuery = URLEncoder.encode(json.toString, "UTF-8")

      ib.baseUrl + encodedQuery
    }
  }

}
