package services

import bbc.curationkit.models.Asset
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WS
import play.api.libs.ws.WS.WSRequestHolder
import services.IsiteQueryBuilderEngine.IsiteQueryBuilder
import builder._

import scala.concurrent.Future

case class Pagination(pageSize: Int, page: Int)
class GuideService(baseUrl: String) {

  val builder = new AssetsBuilder
  def getAssets(query: String, pageSize: Int, page: Int): Future[Seq[Asset]] = {
    val builtQuery = IsiteQueryBuilder(baseUrl).withPageFilter(Pagination(pageSize, page)).withQuery(query)

    val request: WSRequestHolder = WS.url(builtQuery)
      .withRequestTimeout(5000)

    request.get() map { response =>
      if (response.status == 200) {
        builder.toAssets(response.xml, pageSize)
      }
      else {
        Seq.empty
      }
    }
  }
}
