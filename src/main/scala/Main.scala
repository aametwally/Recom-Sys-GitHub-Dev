/**
  * Created by samrudhinayak on 10/27/16.
  */

import play.api.libs.json._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.{Await, Future}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import scala.concurrent.duration._
import sys.process._

  object Main extends App {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val responseFuture =
      Http().singleRequest(HttpRequest(uri = "https://api.github.com/users/square/repos"))
    //println(responseFuture)

    import system.dispatcher

    val response = Await.result(responseFuture, 5.seconds)

    val p = response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String)
    for(a<-p)
      {
        val obj = Json.parse(a)
        println("hello" +obj)
        var firstObj= obj(0)
        var clone_url = firstObj.\("clone_url")
        println("here  " +clone_url)

      }
    //    while(i<20) {
    //      var firstObj = obj(i);
    //      var lang = firstObj.\ ("language")
    //      //println(lang)
    //        i = i + 1
  //}
  }

