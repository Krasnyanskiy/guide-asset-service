package bbc.curationkit.assets

import bbc.curationkit.models._
import bbc.curationkit.writers.Writers
import play.api.libs.json._
import writer.HalResponse.Links

object AssetServiceWrites {

  implicit def myTraversableWrites[A: Writes] = new Writes[Traversable[A]] {
    def writes(items: Traversable[A]) = Json.obj(
      "count" -> items.size,
      "_embedded" -> Json.obj("assets" -> Writes.traversableWrites[A].writes(items)))
  }

  implicit val assetWrites = new Writes[Asset] with Writers {

    def writes(asset: Asset) = {

      val jsAsset: JsObject = Json.toJson(asset).as[JsObject]

      val selfUri: String = ""

      jsAsset ++ Links().withLinks(("self", selfUri, false)).asJson
    }
  }
}

