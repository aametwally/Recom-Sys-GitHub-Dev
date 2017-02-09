
import akka.actor.Actor
import akka.stream.ActorMaterializer
import sys.process._
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.io._
import java.util.function.Consumer
import org.jgrapht.graph._
import java.util.HashMap
import java.util.Set

import com.scitools.understand._

object AnalyzerActor {

  class ComparisonActor extends Actor {
    implicit val materializer = ActorMaterializer()
    var index_for_old = 0
    var versionnames: Array[String] = Array("","")
    var num_added_nodes = 0
    var num_deleted_nodes = 0
    // probably permanently unused
    var num_same_nodes = 0
    var dg_objs = Array(new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]),
                        new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]))
    var dg_objs_sorted = Array(new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]),
                               new DefaultDirectedGraph[String, DefaultEdge](classOf[DefaultEdge]))
    var added_vertices_array = Array[String]()
    var deleted_vertices_array = Array[String]()
    var called_functions_in_addedfns_list = new HashMap[String,Array[String]]()
    var called_functions_in_deletedfns_list = new HashMap[String,Array[String]]()
    var outgoing_verts = Array[String]()

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

      // parse dependency graphs finding added and deleted vertices
      // ... as well as quantity of added and deleted vertices
      for (vert <- vertex_array0) {
        if (vertex_array1.contains(vert)) {
          // probably permanently unused
          num_same_nodes = num_same_nodes + 1
        }
        else {
          num_deleted_nodes = num_deleted_nodes + 1
          deleted_vertices_array = deleted_vertices_array :+ vert.toString()
          outgoing_verts = Array[String]()
          dg_objs_sorted(0).outgoingEdgesOf(vert.toString()).forEach(new Consumer[DefaultEdge] {
            def accept(s: DefaultEdge) =
              outgoing_verts = outgoing_verts :+ dg_objs_sorted(0).getEdgeTarget(s)
          })
          called_functions_in_deletedfns_list.put(vert.toString(),outgoing_verts)
        }
      }

      for (vert <- vertex_array1) {
        if (!vertex_array0.contains(vert)) {
          num_added_nodes = num_added_nodes + 1
          added_vertices_array = added_vertices_array :+ vert.toString()
          outgoing_verts = Array[String]()
          dg_objs_sorted(1).outgoingEdgesOf(vert.toString()).forEach(new Consumer[DefaultEdge] {
            def accept(s: DefaultEdge) =
              outgoing_verts = outgoing_verts :+ dg_objs_sorted(1).getEdgeTarget(s)
          })
          called_functions_in_addedfns_list.put(vert.toString(),outgoing_verts)
        }
      }
    }

    def writeResultsToFile(repoString:String): Unit =
    {
      // write all final results to text files
      val fdir = repoString + "/Analysis";
      
      var newVersion=  Process(Seq("mkdir", fdir))!!; 
      
      // create/add to function subdirectories here
      val adddir = repoString + "/Analysis/addedfns";
      newVersion=  Process(Seq("mkdir", adddir))!!;
      // create/add to function subdirectories here
      val dldir = repoString + "/Analysis/deletedfns";
      newVersion=  Process(Seq("mkdir", dldir))!!;

      val temp_write_file = new PrintWriter(new File(repoString + "/Analysis" +"/primaryDependencyGraphDifferences.txt"))

      temp_write_file.write("Number of functions added " + num_added_nodes.toString() + "\n")
      temp_write_file.write("Number of deleted functions " + num_deleted_nodes.toString() + "\n")

      temp_write_file.write("\nAdded functions which should be tested:")
      if (added_vertices_array.size == 0)
          temp_write_file.write("none\n")
      for (i <- added_vertices_array)
      {
        temp_write_file.write("Added dependency graph vertex: " + i + "\n")

        val add_temp_writer = new PrintWriter(new File(repoString + "/Analysis" +"/addedfns/" + i + ".txt"))
        add_temp_writer.write("Number of called fns in this fn: " + called_functions_in_addedfns_list.get(i).size + "\n")
        if (called_functions_in_addedfns_list.get(i).size == 0)
            add_temp_writer.write("none\n")
        for (j <- called_functions_in_addedfns_list.get(i))
        {
          add_temp_writer.write("function: " + j + "\n")
        }
        add_temp_writer.close()
      }

      temp_write_file.write("\nDeleted functions (in v1 that aren't in v2):\n")
      if (deleted_vertices_array.size == 0)
        temp_write_file.write("none\n")
      for (i <- deleted_vertices_array)
      {
        temp_write_file.write("Deleted dependency graph vertex: " + i + "\n")

        val del_temp_writer = new PrintWriter(new File(repoString + "/Analysis" +"/deletedfns/" + i + ".txt"))
        del_temp_writer.write("Number of called fns in this fn: " + called_functions_in_deletedfns_list.size() + "\n")
        if (called_functions_in_deletedfns_list.get(i).size == 0)
            del_temp_writer.write("none\n")
        for (j <- called_functions_in_deletedfns_list.get(i))
        {
          del_temp_writer.write("function: " + j + "\n")
        }
        del_temp_writer.close()
      }

      temp_write_file.close()

      println("Done writing to all files...")
    }

    def displayResults(repoString:String): Unit =
    {
      println("Please open " + repoString + "/" + "primaryDependencyGraphDifferences.txt to see the primary results for this project (subfolder in project root directory)")
      println("Examine text files in " + repoString + "/deletedfns/ to see functions called in each deleted function for this project (subfolder in project root directory)")
      println("Examine text files in " + repoString + "/addedfns/ to see functions called in each added function for this project (subfolder in project root directory)")
    }

    def analyzeResults(repoString:String): Unit =
    {
      // analysis part
      setupSortedArray()
      calculateResults()
      writeResultsToFile(repoString)
      displayResults(repoString)
    }

    def receive= {
      case repoString: String => {
        val list_of_version_subfolders = createUDBFiles(repoString)
        createDepGraphs(list_of_version_subfolders)
        analyzeResults(repoString)
      }
    }

  }

}
