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

  object Main extends App {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val uri = "https://api.github.com/users/square/repos"
          val urls = scala.io.Source.fromURL(uri).mkString
        //println(urls)
    val obj = Json.parse(urls)
    println(obj)
    var firstObj = obj(0);
    var clone_url = firstObj.\("clone_url")
    println(clone_url)
//val responseFuture: Future[HttpResponse] =
//Http().singleRequest(HttpRequest(uri = "http://akka.io"))
//
//    import system.dispatcher
//
//    val response = Await.result(responseFuture, 5.seconds)
//
//    response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String).foreach(println)
//    println(response.getClass.getSimpleName)
//
//    val json = EntityUtils.
  }


  //val ans= Await.result(p,5.seconds)

  //println("hello  " +p)

  //println("type   " +p.getClass.getSimpleName)


//      val uri = "https://api.github.com/users/square/repos"
//      val urls = scala.io.Source.fromURL(uri).mkString
//    //println(urls)
//    val ur = urls.replace("[",""""")
//    val u = ur.replace("]","")
//      println(u)
//     //Json.parse(u)
//      val cloner = urls.split(",").toList
//      println(cloner)
//      val patt = "clone_url:"
//    cloner.foreach(println)

