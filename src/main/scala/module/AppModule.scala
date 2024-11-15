package module

import cache.{InMemoryMarvelCache, MarvelCache, RedisMarvelCache}
import client.MarvelClient
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.ConfigFactory
import controller.MarvelController
import service.MarvelService

import javax.inject.Named

class AppModule extends AbstractModule {

  override def configure(): Unit = {
    val useInMemoryCache = config.getBoolean("cache.useInMemory")

    if (useInMemoryCache)
      bind(classOf[MarvelCache]).to(classOf[InMemoryMarvelCache]).asEagerSingleton()
    else
      bind(classOf[MarvelCache]).to(classOf[RedisMarvelCache]).asEagerSingleton()

    bind(classOf[MarvelService]).asEagerSingleton()
    bind(classOf[MarvelClient]).asEagerSingleton()
    bind(classOf[MarvelController]).asEagerSingleton()
  }

  private val config = ConfigFactory.load()

  @Provides @Named("marvelBaseUrl")
  def provideBaseUrl: String = config.getString("marvel.api.baseUrl")

  @Provides @Named("marvelPublicKey")
  def providePublicKey: String = config.getString("marvel.api.publicKey")

  @Provides @Named("marvelPrivateKey")
  def providePrivateKey: String = config.getString("marvel.api.privateKey")

}
