import DatabaseActor.ComparisonActor
import akka.actor.{Actor, ActorSystem, Props}
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
        val actors = system.actorOf(Props[ComparisonActor], "ComparisonActor")
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
            var tag_url = projectJS.\("tags_url").toString().replace("\"", "");
            val responseFuture3 = Http().singleRequest(HttpRequest(uri = tag_url))
            val response3 = Await.result(responseFuture2, Duration.Inf)
            response3.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String).foreach(println)
            projectsCloneURL += cloneURLtmp
            cloneGitHubStr="git clone " + cloneURLtmp + " repo_projects/" + projectFullName
            val Array(n1, n2, _*) = projectFullName.split("/")
            var repostring = "repo_projects/" + n1
            println("cloneURLtmp="+cloneURLtmp)
            println("projectFullName=" + projectFullName)
            println("Clone command = " + cloneGitHubStr)
            println("Repo location = " +repostring)

            val yy =  cloneGitHubStr !!;
            var urlss = scala.io.Source.fromURL(tag_url).mkString
            var obj = Json.parse(urlss)
            val projectVersion = scala.collection.mutable.MutableList[String]()

            // Extract the older version of the product
            var firstObj = obj(1);
            var tag = firstObj.\("commit").\("sha").toString().replace("\"", "");
            println(tag)

//            for(c <- projectVersion)
//              Add a comment to this line
//                {
//                  println(c)
//                }

            println("Finish getting the versions")


            var v2Name :  String = "repo_projects/" + projectFullName + "_v2"
            println("v2name= " + v2Name)
            var newVersion=   Process(Seq("mkdir", v2Name))!!; //"mkdir $v2Name"!!
          var checkoutCommand =  "git --git-dir=repo_projects/" + projectFullName + "/.git --work-tree=" + v2Name + " checkout " + tag + " -- ."
            println("version command = " + checkoutCommand)
            var tt = checkoutCommand!!

            println("I am here now")
            actors ! repostring


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
