/**
  * Created by samrudhinayak on 10/27/16.
  */

//import SimpleActor.ComplexActor
import SimpleActor.ComplexActor
import play.api.libs.json._
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.{Await, Future}
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.duration._
import sys.process._
object Main1 {

  class Main1 extends App {

    implicit val system = ActorSystem("Main Actor")
    implicit val materializer = ActorMaterializer()

    val responseFuture =
      Http().singleRequest(HttpRequest(uri = "https://api.github.com/users/square/repos"))
    //println(responseFuture)

    import system.dispatcher

    val response = Await.result(responseFuture, Duration.Inf)

    val p = response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String)
    var i = 0
    for (a <- p) {
      while (i < 20) {
        val obj = Json.parse(a)
        //println("hello" + obj)
        var firstObj = obj(i)
        i = i + 1
        var clone_url = firstObj.\("clone_url")
        //println("here  " +clone_url)
        val system = ActorSystem("SystemActor")
        //val actor = system.actorOf(Props[SimpleActor], "SimpleActor")
        val actors = system.actorOf(Props[ComplexActor], "ComplexActor")
        actors ! clone_url
        //actors ! clone_url.toString()
        //actors ! 567
      }
    }
    //    while(i<20) {
    //      var firstObj = obj(i);
    //      var lang = firstObj.\ ("language")
    //      //println(lang)
    //        i = i + 1
    //}
  }

}

