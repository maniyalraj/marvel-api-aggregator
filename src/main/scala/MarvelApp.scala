import com.google.inject.Guice
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}
import com.typesafe.config.ConfigFactory
import controller.MarvelController
import module.AppModule

object MarvelApp {

  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(new AppModule)
    val config = ConfigFactory.load()
    val port = config.getInt("server.port")

    val marvelController = injector.getInstance(classOf[MarvelController])

    val service = new com.twitter.finagle.Service[Request, Response] {
      def apply(request: Request): Future[Response] = {
        marvelController.handleRequest(request)
      }
    }

    val server = Http.serve(s":$port", service)
    Await.ready(server)
  }
}
