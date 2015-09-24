package builder

import bbc.curationkit.models.{Asset, Attribution, Image}
import play.api.Logger

import scala.util.{Failure, Success, Try}
import scala.xml.Node

case class ImageChefUrl(url: String) {
  override def toString = url
}

class AssetsBuilder {

  val imageChefUrl = "ichef.test.bbci.co.uk/images/ic/$recipe/$imagePid.jpg"
  val guidePrefix = "http://test.bbc.co.uk/guides/"
  val imageChefRecipe = "240x135"

  private[this] val log = Logger(getClass)

  def toAssets(xml: Node, limit: Int): Seq[Asset] = {

    val t: Seq[Try[Asset]] = (xml \ "results" \ "_").map{ node =>
      val result = toAsset(node)

      result match {
        case Success(_) => // Success cases are needed later
        case Failure(e) => log.warn("Guide asset could not be parsed", e)
      }

      result
    }

    t.filter{_.isSuccess }.map{_.get}.take(limit)
  }

  def toAsset(node: Node): Try[Asset] = Try {

    Asset(
      id = GuideAssetBuilder.IdPrefix + getId(node),
      label = getLabel(node),
      imageUrl = getImageUrl(node),
      url = guidePrefix + getId(node),
      image = getImage(node),
      assetType = "guide",
      description = getDescription(node),
      attribution = getAttribution(),
      contentData = None,
      Seq.empty
    )
  }

  private def getId(node: Node) =
    (node \ "document" \ "guide" \ "summary" \ "id").text

  private def getLabel(node: Node) = {
    (node \ "document" \ "guide" \ "summary" \ "title").text
  }

  private def getImageUrl(node: Node) =
    (node \ "document" \ "guide" \ "summary" \ "meta" \ "pid").headOption map { imagePid =>
      s"http://$imageChefUrl".replaceFirst("\\$recipe", imageChefRecipe).replaceFirst("\\$imagePid", imagePid.text)
    }

  private def getImage(node: Node) =
    (node \ "document" \ "guide" \ "summary" \ "meta" \ "pid").headOption map { imagePid =>
      Image(templateUrl = Option(s"$imageChefUrl".replaceFirst("\\$imagePid", imagePid.text)))
    }

  private def getDescription(node: Node) =
    (node \ "document" \ "guide" \ "summary" \ "meta" \ "description").headOption map { desc => desc.text }

  private def getAttribution() = Attribution(Some("iWonder"), Some("http://bbc.co.uk/iwonder"))
}

object GuideAssetBuilder {
  val IdPrefix = "urn:bbc:guide:"
}
