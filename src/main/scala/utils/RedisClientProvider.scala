package utils

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory

class RedisClientProvider {

  private val config = ConfigFactory.load()

  private val host = config.getString("redis.host")
  private val port = config.getInt("redis.port")
  private val password = if (config.hasPath("redis.password")) Some(config.getString("redis.password")) else None
  private val redisDb = if (config.hasPath("redis.db")) config.getInt("redis.db") else 0

  val redisClient: RedisClient = new RedisClient(host, port, database = redisDb, secret = password)

}
