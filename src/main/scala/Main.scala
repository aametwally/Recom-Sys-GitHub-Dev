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

  /// User specify the language and the keyword
  val lang = scala.io.StdIn.readLine("Enter the Language > ")
  println("Language= "+ lang)
  val keyword = scala.io.StdIn.readLine("Enter the keyword of the project > ")
  println("keyword= "+ keyword)
  var searchPath= "https://api.github.com/legacy/repos/search/"+keyword+"?language="+lang
  println("searchPath=" + searchPath)

  // send akka http request to get list of projects with the specified language and keyword
//  val responseFuture = Http().singleRequest(HttpRequest(uri = searchPath))
  import system.dispatcher
//  val response = Await.result(responseFuture, Duration.Inf) // 5.seconds)
//  val p = response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String)
//  val projectsURL = scala.collection.mutable.MutableList[String]()
//  var i =0;
//  for(m<-p)
//  {
//    i+=1
//    println(i)
//    val obj = Json.parse(m)
//    var tempstr : String = null
//    var username : String = null
//    var name : String = null
//    for( a <- 0 to 5){
//      username = obj.\("repositories")(a).\("username").toString()
//      name = obj.\("repositories")(a).\("name").toString()
//      username= username.replace("\"", "");
//      name= name.replace("\"", "");
//      tempstr="https://api.github.com/repos/"+username+"/"+name
//      println(username)
//      println(name)
//      println("tempStr= " + tempstr)
//      projectsURL+= "https://api.github.com/repos/"+username+"/"+name
//    }
//
//
//  }
//
//  println("finish the first http request")

  val projectsURL = scala.collection.mutable.MutableList[String]()
  projectsURL+="https://api.github.com/repos/HCBravoLab/MicrobiomeSC"

  //TODO: (Bug) This part is processed before the wait time finish.
  val projectsCloneURL = scala.collection.mutable.MutableList[String]()
  var cloneURLtmp : String = null
  var projectFullName : String = null
  var cloneGitHubStr : String = null
  var j=0
  for (path <- projectsURL)
  {
    j+=1
    println("j= "+j)
    println("here is the for= " +path )

    val responseFuture2 = Http().singleRequest(HttpRequest(uri = path))
    val response2 = Await.result(responseFuture2, Duration.Inf)
    val p2 = response2.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String)
    //var projectFullName
    for(m2<-p2)
    {
      val projectJS = Json.parse(m2)
      projectFullName=projectJS.\("full_name").toString()
      projectFullName = projectFullName.replace("\"", "");
      cloneURLtmp = projectJS.\("clone_url").toString()
      cloneURLtmp = cloneURLtmp.replace("\"", "");
      projectsCloneURL += cloneURLtmp
      cloneGitHubStr="git clone " + cloneURLtmp + " repo_projects/" + projectFullName

      println("cloneURLtmp="+cloneURLtmp)
      println("projectFullName=" + projectFullName)
      println("Clone command = " + cloneGitHubStr)


      val yy =  cloneGitHubStr !
    }
  }


  projectsCloneURL.foreach{println}
  println("finish")
}

