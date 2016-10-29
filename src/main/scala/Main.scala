/**
  * Created by samrudhinayak on 10/27/16.
  */
import akka.actor.ActorSystem
import akka.util.ByteString

import scala.concurrent.{Await, Future}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import scala.concurrent.duration._

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val responseFuture: Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = "https://api.github.com/users/square/repos"))

  import system.dispatcher

  val response = Await.result(responseFuture, 10.seconds)

  response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String).foreach(println)
  
}
