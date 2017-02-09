import AnalyzerActor.ComparisonActor
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
object DownloaderActor{

  class ClonerActor extends Actor{
    implicit val materializer = ActorMaterializer()
    implicit val system = ActorSystem("SimpleActor")
    val actors = system.actorOf(Props[ComparisonActor], "ComparisonActor")
    def receive = {
      case projectsURL: String => {
        import system.dispatcher
        //println("String : " + projectsURL)
        implicit val system = ActorSystem("SimpleActor")
        val actors = system.actorOf(Props[ComparisonActor], "ComparisonActor")
        val projectsCloneURL = scala.collection.mutable.MutableList[String]()
        val projectsURLs = scala.collection.mutable.MutableList[String]()
        projectsURLs += projectsURL
        var cloneURLtmp : String = null
        var projectFullName : String = null
        var cloneGitHubStr : String = null
        var j=0
        for (path <- projectsURLs) {
          val responseFuture2 = Http().singleRequest(HttpRequest(uri = path))
          val response2 = Await.result(responseFuture2, Duration.Inf)
          val p2 = response2.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String)
          for(m2<-p2)
          {
            val projectJS = Json.parse(m2)
            projectFullName=projectJS.\("full_name").toString()
            projectFullName = projectFullName.replace("\"", "");
            cloneURLtmp = projectJS.\("clone_url").toString()
            cloneURLtmp = cloneURLtmp.replace("\"", "");
            var tag_url = projectJS.\("tags_url").toString().replace("\"", "");
            //println("tags  " + tag_url)
            val responseFuture3 = Http().singleRequest(HttpRequest(uri = tag_url))
            val response3 = Await.result(responseFuture3, Duration.Inf)
            val temptag = response3.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String)
            for(s<-temptag)
              {
                val jtemp = Json.parse(s)
                val tempstr = s.toString.length
                if(tempstr > 2) {
                  //println(" sammy " + tempstr)
                  projectsCloneURL += cloneURLtmp
                  cloneGitHubStr = "git clone " + cloneURLtmp + " repo_projects/" + projectFullName
                  val Array(n1, n2, _*) = projectFullName.split("/")
                  var repostring = "repo_projects/" + n1
                  parsing(repostring, cloneURLtmp, projectFullName, cloneGitHubStr, tag_url)
                }
              }
            //if(temptag.toString.length>0) println("Tags exist   " + temptag.toString.length)

          }
          //println("Done")
        }


        //projectsCloneURL.foreach{println}
        //println("finish")
      }
      case i:Int => println("Integer : " +i)

    }
    def parsing(repostring: String, cloneURLtmp: String, projectFullName: String, cloneGitHubStr: String, tag_url: String ) = {
//      println("cloneURLtmp="+cloneURLtmp)
//      println("projectFullName=" + projectFullName)
//      println("Clone command = " + cloneGitHubStr)
//      println("Repo location = " +repostring)

      val yy =  cloneGitHubStr !!;
      var urlss = scala.io.Source.fromURL(tag_url).mkString
      var obj = Json.parse(urlss)
      val projectVersion = scala.collection.mutable.MutableList[String]()

      // Extract the older version of the product
      var firstObj = obj(1);
      var tag = firstObj.\("commit").\("sha").toString().replace("\"", "");
//      println(tag)
//      println("Finish getting the versions")


      var v2Name :  String = "repo_projects/" + projectFullName + "_v2"
      //println("v2name= " + v2Name)
      var newVersion=   Process(Seq("mkdir", v2Name))!!; //"mkdir $v2Name"!!
      var checkoutCommand =  "git --git-dir=repo_projects/" + projectFullName + "/.git --work-tree=" + v2Name + " checkout " + tag + " -- ."
      //println("version command = " + checkoutCommand)
      var tt = checkoutCommand!!

     // println("I am here now")
      actors ! repostring
    }
    //def foo = println("Normal method")
  }

}
