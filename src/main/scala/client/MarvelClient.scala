package client

import com.google.inject.Inject
import com.google.inject.name.Named
import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import io.circe.parser._
import models.{Character, Comic}
import org.slf4j.LoggerFactory

import java.net.URLEncoder

class MarvelClient @Inject() (
  @Named("marvelPublicKey") publicKey: String,
  @Named("marvelPrivateKey") privateKey: String,
  @Named("marvelBaseUrl") baseURL: String
) {
  private val client =
    Http.client.withTls("gateway.marvel.com").newService("gateway.marvel.com:443")
  private val log = LoggerFactory.getLogger(getClass)

  private def generateHash(ts: String): String = {
    val toHash = s"$ts$privateKey$publicKey"
    java.security.MessageDigest
      .getInstance("MD5")
      .digest(toHash.getBytes)
      .map("%02x".format(_))
      .mkString
  }

  def getCharacter(name: String): Future[Option[Character]] = {
    val ts = System.currentTimeMillis.toString
    val hash = generateHash(ts)
    val url =
      s"$baseURL/characters?name=${URLEncoder.encode(name, "UTF-8")}&apikey=$publicKey&ts=$ts&hash=$hash"
    log.info(s"Sending request to Marvel API: $url")

    val request = Request(url)

    client(request)
      .flatMap { response =>
        log.debug(s"Received response with status: ${response.statusCode}")
        parse(response.contentString) match {
          case Right(json) =>
            json.hcursor.downField("data").downField("results").as[List[Character]] match {
              case Right(characters) => Future.value(characters.headOption)
              case Left(decodingError) =>
                log.error(s"Failed to decode character data: ${decodingError.getMessage}")
                Future.value(None)
            }
          case Left(parsingError) =>
            log.error(s"Failed to parse response: ${parsingError.getMessage}")
            Future.value(None)
        }
      }
      .rescue { case e: Throwable =>
        log.error(s"Failed to fetch character due to exception: ${e.getMessage}", e)
        Future.value(None)
      }
  }

  private def getComics(
    characterId: Int,
    startYear: Int,
    orderBy: String,
    limit: Int = 20,
    offset: Int = 0
  ): Future[List[Comic]] = {
    val ts = System.currentTimeMillis.toString
    val hash = generateHash(ts)
    val url =
      s"$baseURL/characters/$characterId/comics?startYear=$startYear&orderBy=$orderBy&limit=$limit&offset=$offset&apikey=$publicKey&ts=$ts&hash=$hash"
    log.info(s"Sending request to Marvel API: $url")
    val request = Request(url)

    client(request)
      .flatMap { response =>
        parse(response.contentString) match {
          case Right(json) =>
            json.hcursor.downField("data").downField("results").as[List[Comic]] match {
              case Right(comics) => Future.value(comics)
              case Left(decodingError) =>
                log.error(s"Failed to decode comics data: ${decodingError.getMessage}")
                Future.value(List.empty)
            }
          case Left(parsingError) =>
            log.error(s"Failed to parse response: ${parsingError.getMessage}")
            Future.value(List.empty)
        }
      }
      .rescue { case e: Throwable =>
        log.error(s"Failed to fetch comics due to exception: ${e.getMessage}", e)
        Future.value(List.empty)
      }
  }

  def fetchAllComics(
    characterId: Int,
    startYear: Int,
    orderBy: String,
    maxResults: Option[Long] = Some(
      40L
    ) // default cutoff of 40 comics to avoid unexpected fetching of huge data
  ): Future[List[Comic]] = {
    def fetchWithPagination(offset: Int, acc: List[Comic]): Future[List[Comic]] = {
      getComics(characterId, startYear, orderBy, offset = offset).flatMap { comics =>
        if (comics.isEmpty || maxResults.exists(max => acc.size.toLong >= max)) Future.value(acc)
        else fetchWithPagination(offset + 20, acc ++ comics)
      }
    }

    fetchWithPagination(0, Nil)
  }
}
