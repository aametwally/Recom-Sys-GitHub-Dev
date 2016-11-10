import akka.actor.Actor
import akka.stream.ActorMaterializer
import sys.process._
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.io._
import java.util.function.Consumer
import org.jgrapht.DirectedGraph
import org.jgrapht.alg._
import org.jgrapht.graph._
import org.jgrapht.util._

import com.scitools.understand._

/**
  * Created by samrudhinayak on 11/8/16.
  */
object DatabaseActor extends App {

  class ComparisonActor extends Actor {
    implicit val materializer = ActorMaterializer()
    def receive= {
      case repoString: String => {
        println("Repo string: " + repoString)
        // change these to fit the project
        val test_java_file_path = repoString
        // make generic (java for now)
        val test_java_file_extension = "java"
        val und_path = "/home/hady/Dropbox/UIC/Courses/CS-474-OOP/HW2/scitools/bin/linux64/und "
        val list_of_version_subfolders = new File(test_java_file_path).listFiles.filter(_.isDirectory).map(_.getName)
        list_of_version_subfolders.foreach {
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
            temp_write_file.close()
            val output_file = sub_folder + ".udb"
            val und_commands = "create -languages java add @CurrentProjectJavaList.txt analyze -all -quiet " + output_file
            val singular_execution_command = und_path + und_commands
            val command_to_run = singular_execution_command !
        }
        /*
        FOR EACH UDB FILE (or version): create dependency graphs for each version
        */
        var index_for_old = 0
        val dg_objs = Array(new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]),
          new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]))

        val versionnames: Array[String] = list_of_version_subfolders

        if (versionnames(0).substring(versionnames(0).length-3).equals("_v2"))
        {
          index_for_old = 0
        }
        else
        {
          index_for_old = 1
        }

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
        var num_added_nodes = 0
        var num_deleted_nodes = 0
        var num_same_nodes = 0

        val dg_objs_sorted = Array(new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]),
          new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]))

        if (index_for_old == 0)
        {
          dg_objs_sorted(0) = dg_objs(0)
          dg_objs_sorted(1) = dg_objs(1)
        }
        else
        {
          dg_objs_sorted(0) = dg_objs(1)
          dg_objs_sorted(1) = dg_objs(0)
        }

        var added_vertices_array = Array[String]()
        var deleted_vertices_array = Array[String]()

        val vertex_array0 = dg_objs_sorted(0).vertexSet().toArray()
        val vertex_array1 = dg_objs_sorted(1).vertexSet().toArray()

        for (vert <- vertex_array0)
        {
          if (vertex_array1.contains(vert))
          {
            num_same_nodes = num_same_nodes + 1
          }
          else
          {
            num_deleted_nodes = num_deleted_nodes + 1
            deleted_vertices_array = deleted_vertices_array :+ vert.toString()
          }
        }

        for (vert <- vertex_array1)
        {
          if (!vertex_array0.contains(vert))
          {
            num_added_nodes = num_added_nodes + 1
            added_vertices_array = added_vertices_array :+ vert.toString()
          }
        }

        println("Added vertices:")
        for (i <- added_vertices_array)
        {
          println("Added vertex: " + i)
        }

        println("Deleted vertices:")
        for (i <- deleted_vertices_array)
        {
          println("Deleted vertex: " + i)
        }

        println("")
        println("Num added: " + num_added_nodes.toString())
        println("Num deleted: " + num_deleted_nodes.toString())

      }
    }

  }

}
