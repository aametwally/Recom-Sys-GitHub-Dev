
import akka.actor.Actor
import akka.stream.ActorMaterializer
import sys.process._
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.io._
import java.util.function.Consumer
import org.jgrapht.graph._

import com.scitools.understand._

object AnalyzerActor {

  class ComparisonActor extends Actor {
    implicit val materializer = ActorMaterializer()
    var index_for_old = 0
    var versionnames: Array[String] = Array("","")
    var num_added_nodes = 0
    var num_deleted_nodes = 0
    var num_same_nodes = 0
    var dg_objs = Array(new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]),
      new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]))
    var dg_objs_sorted = Array(new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]),
      new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]))
    var added_vertices_array = Array[String]()
    var deleted_vertices_array = Array[String]()

    // create 2 udb files (one for each version)
    def createUDBFiles (repoString:String): Array[String] = {
      val test_java_file_path = repoString
      // make generic (java for now)
      val test_java_file_extension = "java"
      val list_of_version_subfolders = new File(test_java_file_path).listFiles.filter(_.isDirectory).map(_.getName)
      // for each of the 2 subfolders
      list_of_version_subfolders.foreach {
        // create a udb for every java file in that subdirectory (and nested subdirectories)
        sub_folder =>
          val temp_write_file = new PrintWriter(new File("CurrentProjectJavaList.txt"))
          // Some of this code was used in hw1 (this contains scala equivalent of that code) (Eric Wolfson)
          Files.walk(Paths.get(test_java_file_path + "/" + sub_folder)).forEach(new Consumer[Path] {
            def accept(pth: Path) =
              if (Files.isRegularFile(pth)) {
                if (pth.toString().substring(pth.toString().lastIndexOf('.') + 1).equals(test_java_file_extension)) {
                  temp_write_file.write(pth.toString() + "\n")
                }
              }
          })
          // execute und for udb file
          temp_write_file.close()
          val output_file = sub_folder + ".udb"
          val und_commands = "create -languages java add @CurrentProjectJavaList.txt analyze -all -quiet " + output_file
          val singular_execution_command = "und " + und_commands
          val command_to_run = singular_execution_command !
      }
      return list_of_version_subfolders
    }

    // create and return 2 dependency graphs
    def createDepGraphs (list_of_version_subfolders:Array[String]): Unit =
    {
      versionnames = list_of_version_subfolders
      val num_versions = 2
      for (ver <- 0 until num_versions) {
        val db_obj = Understand.open(versionnames(ver) + ".udb")
        val funcns: Array[Entity] = db_obj.ents("method ~unknown ~unresolved")
        var nodeadded = ""
        var nodeconnected = ""
        for (ent <- funcns) {
          // type ticks needed for scala (unlike java)
          nodeadded = ent.`type`() + " " + ent.name()
          if (!dg_objs(ver).containsVertex(nodeadded)) {
            dg_objs(ver).addVertex(nodeadded)
          }
          val paramrefs: Array[Reference] = ent.refs("Call", "Method", true)
          for (prf <- paramrefs) {
            val pent = prf.ent()
            // type ticks needed for scala (unlike java)
            nodeconnected = pent.`type`() + " " + pent.name()
            if (!dg_objs(ver).containsVertex(nodeconnected)) {
              dg_objs(ver).addVertex(nodeconnected)
            }
            dg_objs(ver).addEdge(nodeadded, nodeconnected)
          }
        }
        db_obj.close()
      }
    }

    def setupSortedArray(): Unit =
    {
      if (versionnames(0).substring(versionnames(0).length-3).equals("_v2"))
      {
        index_for_old = 0
      }
      else
      {
        index_for_old = 1
      }

      if (index_for_old == 0) {
        dg_objs_sorted(0) = dg_objs(0)
        dg_objs_sorted(1) = dg_objs(1)
      }
      else {
        dg_objs_sorted(0) = dg_objs(1)
        dg_objs_sorted(1) = dg_objs(0)
      }
    }

    def calculateResults(): Unit =
    {
      val vertex_array0 = dg_objs_sorted(0).vertexSet().toArray()
      val vertex_array1 = dg_objs_sorted(1).vertexSet().toArray()

      for (vert <- vertex_array0) {
        if (vertex_array1.contains(vert)) {
          num_same_nodes = num_same_nodes + 1
        }
        else {
          num_deleted_nodes = num_deleted_nodes + 1
          deleted_vertices_array = deleted_vertices_array :+ vert.toString()
        }
      }

      for (vert <- vertex_array1) {
        if (!vertex_array0.contains(vert)) {
          num_added_nodes = num_added_nodes + 1
          added_vertices_array = added_vertices_array :+ vert.toString()
        }
      }
    }

    def displayResults(): Unit =
    {
      println("Added functions which can be tested:")
      for (i <- added_vertices_array) {
        println("Added vertex: " + i)
      }
      println(" ")
      println("Deleted functions different from previous version:")
      for (i <- deleted_vertices_array) {
        println("Deleted vertex: " + i)
      }

      println("STATS:")
      println("Number of functions added: " + num_added_nodes.toString())
      println("Number of functions deleted: " + num_deleted_nodes.toString())
      println(" ")
      println(" ")
    }

    def analyzeResults(): Unit =
    {
      // analysis part
      setupSortedArray()
      calculateResults()
      displayResults()
    }

    def receive= {
      case repoString: String => {
        val list_of_version_subfolders = createUDBFiles(repoString)
        createDepGraphs(list_of_version_subfolders)
        analyzeResults()
      }
    }

  }

}