import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.util.ByteString
import play.api.libs.json.Json

import sys.process._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by samrudhinayak on 10/31/16.
  */
object SimpleActor extends App{

  class ComplexActor extends Actor{
    implicit val materializer = ActorMaterializer()
    def receive = {
      case projectsURL: String => {
        import system.dispatcher
        println("String : " + projectsURL)
        implicit val system = ActorSystem("SimpleActor")
        val projectsCloneURL = scala.collection.mutable.MutableList[String]()
        val projectsURLs = scala.collection.mutable.MutableList[String]()
        projectsURLs += projectsURL
        var cloneURLtmp : String = null
        var projectFullName : String = null
        var cloneGitHubStr : String = null
        var j=0
        //println("here  " +projectsURL)
        //println("another " +projectsURLs)
        for (path <- projectsURLs) {
          //println("I am here " + path)

          //          j+=1
          //          println("j= "+j)
          //          println("here is the for= " +path )
          //
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
          println("Done")
        }


        projectsCloneURL.foreach{println}
        println("finish")
      }
      case i:Int => println("Integer : " +i)

    }
    def foo = println("Normal method")
  }

}
