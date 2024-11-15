package cache

import com.github.blemale.scaffeine.{Cache, Scaffeine}
import com.google.inject.Inject
import com.typesafe.config.ConfigFactory
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import models.{Character, Comic}
import utils.RedisClientProvider

import scala.concurrent.duration._

trait MarvelCache {

  private val config = ConfigFactory.load()

  val cacheExpiry: FiniteDuration = config.getInt("cache.expirationInMinutes").minutes

  def get(key: String): Option[(Character, List[Comic])]

  def put(key: String, data: (Character, List[Comic])): Unit
}

class RedisMarvelCache @Inject() (redisClientProvider: RedisClientProvider) extends MarvelCache {

  private val redisClient = redisClientProvider.redisClient

  def get(key: String): Option[(Character, List[Comic])] = redisClient.get[String](key) match {
    case Some(json) => decode[(Character, List[Comic])](json).toOption
    case None       => None
  }

  def put(key: String, data: (Character, List[Comic])): Unit =
    redisClient.setex(key, cacheExpiry.toMinutes, data.asJson.noSpaces)
}

class InMemoryMarvelCache extends MarvelCache {

  private val cache: Cache[String, (Character, List[Comic])] = Scaffeine()
    .expireAfterWrite(cacheExpiry)
    .build[String, (Character, List[Comic])]()

  override def get(key: String): Option[(Character, List[Comic])] = cache.getIfPresent(key)

  override def put(key: String, data: (Character, List[Comic])): Unit = cache.put(key, data)
}
