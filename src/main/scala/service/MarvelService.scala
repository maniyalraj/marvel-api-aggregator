package service

import cache.MarvelCache
import client.MarvelClient
import com.google.inject.Inject
import com.twitter.util.Future
import models.{Character, Comic}
import org.slf4j.LoggerFactory

class MarvelService @Inject() (client: MarvelClient, cache: MarvelCache) {

  private val log = LoggerFactory.getLogger(getClass)

  def fetchCharacterWithComicsData(
    name: String,
    comicYear: Int,
    orderBy: String
  ): Future[Option[(Character, List[Comic])]] = {
    log.info(s"Fetching data for character: $name, year: $comicYear, orderBy: $orderBy")

    val cacheKey = s"$name-$comicYear-$orderBy"

    cache.get(cacheKey) match {
      case Some((character, comics)) =>
        log.debug(s"Fetched data from cache: ${character.name}, ${comics.size}")
        Future.value(Some(character, comics))
      case None =>
        client.getCharacter(name).flatMap {
          case Some(character) =>
            log.debug(s"Fetched character from API: ${character.name}")
            client.fetchAllComics(character.id, comicYear, orderBy).map { comics =>
              cache.put(cacheKey, (character, comics))
              log.debug(s"Fetched comics from API: ${comics.size}")
              Some(character, comics)
            }
          case None =>
            log.debug(s"Character not found: $name")
            Future.value(None)
        }
    }
  }
}
