package controller

import com.google.inject.Inject
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.circe.syntax.EncoderOps
import models.{Character, Comic}
import service.MarvelService
import org.slf4j.LoggerFactory

import java.time.LocalDate

class MarvelController @Inject() (marvelService: MarvelService) {

  private val log = LoggerFactory.getLogger(getClass)

  def handleRequest(request: Request): Future[Response] = {
    log.info(s"Handling request for path: ${request.path}")

    request.path match {
      case "/marvel-characters" =>
        log.info(s"Fetching Marvel characters with query: ${request.params}")
        getMarvelCharacters(request)
      case _ =>
        log.warn(s"Invalid path: ${request.path}")
        val response = Response()
        response.statusCode(404)
        response.setContentString("Not Found")
        Future.value(response)
    }
  }

  private def getMarvelCharacters(request: Request): Future[Response] = {
    val name = request.getParam("name")
    val comicYear = request.getIntParam("comicYear", LocalDate.now().getYear - 10)
    val orderBy = request.getParam("orderBy", "title")

    if (name == null || name.isEmpty) {
      val response = Response()
      response.statusCode(400)
      response.setContentString("Parameter 'name' is required")
      Future.value(response)
    } else {
      marvelService.fetchCharacterWithComicsData(name, comicYear, orderBy).map { result =>
        val response = Response()
        result match {
          case Some(data) => response.setContentString(serializeToJson(data._1, data._2))
          case None =>
            log.debug(
              s"No Character found for name: $name, comicYear: $comicYear, orderBy: $orderBy"
            )
            response.statusCode(404)
            response.setContentString(s"""{"error": "Character not found"}""")
        }
        response
      }
    }
  }

  private def serializeToJson(character: Character, comics: List[Comic]): String = {
    Map(
      "character" -> character.asJson,
      "comics" -> comics.asJson
    ).asJson.noSpaces
  }
}
