package controller

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Await, Future}
import io.circe.syntax.EncoderOps
import models.{Character, Comic, Thumbnail}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import service.MarvelService

import java.time.LocalDateTime

class MarvelControllerTest extends AnyFunSuite with Matchers with MockitoSugar {

  private val mockMarvelService = mock[MarvelService]
  private val controller = new MarvelController(mockMarvelService)

  test("handleRequest should return 404 for unknown path") {
    val request = Request("/unknown-path")
    val response: Response = Await.result(controller.handleRequest(request))

    response.status shouldEqual Status.NotFound
    response.contentString shouldEqual "Not Found"
  }

  test("getMarvelCharacters should return 400 if 'name' parameter is missing") {
    val request = Request("/marvel-characters")
    val response: Response = Await.result(controller.handleRequest(request))

    response.status shouldEqual Status.BadRequest
    response.contentString shouldEqual "Parameter 'name' is required"
  }

  test(
    "getMarvelCharacters should return character and comics data if valid 'name' parameter is provided"
  ) {
    val character = Character(
      1,
      "Spider-Man",
      "A superhero with spider-like abilities",
      Thumbnail("http://example.com/spiderman", "jpeg")
    )
    val comics = List(
      Comic("The Amazing Spider-Man #1", "1", Some(LocalDateTime.parse("2014-01-01T12:00:00"))),
      Comic("Spider-Man: Homecoming", "2", Some(LocalDateTime.parse("2014-01-01T12:00:00")))
    )

    when(mockMarvelService.fetchCharacterWithComicsData(any[String], any[Int], any[String]))
      .thenReturn(Future.value(Some((character, comics))))

    val request = Request("/marvel-characters?name=Spider-Man&comicYear=2014&orderBy=title")

    val response: Response = Await.result(controller.handleRequest(request))

    val expectedResponse: String = Map(
      "character" -> character.asJson,
      "comics" -> comics.asJson
    ).asJson.noSpaces

    response.status shouldEqual Status.Ok
    response.contentString shouldEqual expectedResponse
  }

  test("getMarvelCharacters should return 404 if character not found") {
    when(mockMarvelService.fetchCharacterWithComicsData(any[String], any[Int], any[String]))
      .thenReturn(Future.None)

    val request = Request("/marvel-characters?name=UknownCharacter")

    val response: Response = Await.result(controller.handleRequest(request))

    response.status shouldEqual Status.NotFound
    response.contentString shouldEqual """{"error": "Character not found"}"""
  }
}
