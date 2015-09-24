package controllers

import bbc.curationkit.readers.Readers
import play.api.libs.json.Json
import play.api.mvc._
import services.GuideService

object Application extends Controller with Readers {
  import bbc.curationkit.assets.AssetServiceWrites._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  private val AcceptsHalJson = Accepting("application/hal+json")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def find(query: String, page_size: Int, page: Int): Action[AnyContent] = Action.async { implicit request =>

    val baseUrl = "https://api.test.bbc.co.uk/isite2-content-reader/search?q="

    val guideService = new GuideService(baseUrl)

    render.async {
      case AcceptsHalJson() =>
        guideService.getAssets(query, page_size, page).map {
          assets => Ok(Json.toJson(assets))
        }
    }
  }
}
