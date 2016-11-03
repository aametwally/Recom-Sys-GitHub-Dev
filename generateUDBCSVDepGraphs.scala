import sys.process._
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.io._
import java.util.function.Consumer

import com.scitools.understand._

// This file generates a udb file. Then generates a csv file based on the dependencies
// Reads from the csv file to generate an adjacency list (for the directed graph representing the dependencies)
// From this adjacency list, we can do analysis

// NEW: Generates a list of 20 udb files for 20 projects under Projects directory
// generates csv files from these udb files
// todo: finish making dependency graphs

object Main extends App
{
    //var total_str = ""
    // change these to fit the project
    var test_java_file_path = ".\\Projects"
    // make generic (java for now)
    var test_java_file_extension = "java"
    var und_path = "C:\\Program Files\\SciTools\\bin\\pc-win64\\und "
    var list_of_subfolders = new File(test_java_file_path).listFiles.filter(_.isDirectory).map(_.getName)
    list_of_subfolders.foreach { sub_folder =>
      val temp_write_file = new PrintWriter(new File("CurrentProjectJavaList.txt"))
      // Some of this code was used in hw1 (this contains scala equivalent of that code) (Eric Wolfson)
      Files.walk(Paths.get(test_java_file_path + "\\" + sub_folder)).forEach(new Consumer[Path] {
        def accept(pth: Path) =
          if (Files.isRegularFile(pth)) {
            if (pth.toString().substring(pth.toString().lastIndexOf('.') + 1).equals(test_java_file_extension)) {
              temp_write_file.write(pth.toString() + "\n")
            }
          }
      })
      temp_write_file.close()
      val output_file = sub_folder + ".udb"
      val und_commands = "create -languages java add @CurrentProjectJavaList.txt analyze -all " + output_file
      val singular_execution_command = und_path + und_commands
      val command_to_run = singular_execution_command !
    }
    list_of_subfolders.foreach
    {
      sub_folder =>
      val und_export_csv = "export -dependencies class csv " + sub_folder + ".csv " + sub_folder + ".udb"
      val csv_command = und_path + und_export_csv !
    }
  /*
    val source_file = io.Source.fromFile(sub_folder + ".csv")
    for (line <- source_file.getLines)
    {
      // inspired by http://alvinalexander.com/scala/csv-file-how-to-process-open-read-parse-in-scala
      val columns = line.split(",").map(_.trim())
      for (i <- 0 until 5)
        print(s"|${columns(i)}|")
      //adjacency_list({columns(0)}) =
      print("\n")
    }
    source_file.close
    // should eventually be the same as output file
    // larger file used here to generate a decent csv file

    //var singular_execution_command = und_path + und_commands
    //val command_to_run = singular_execution_command !
    //var db_obj = Understand.open(output_file)

    //var funcns:Array[Entity] = db_obj.ents("function ~unknown ~unresolved,method ~unknown ~unresolved")
   // var clsses:Array[Entity] = db_obj.ents("class ~unknown ~unresolved")
    for(ent <- clsses)
    {
      val paramList = new StringBuilder()
      val paramrefs:Array[Reference] = ent.refs("define", "parameter", true)
      for (prf <- paramrefs)
      {
        val pent = prf.ent()
        // type ticks needed for scala (unlike java)
        paramList.append(pent.`type`() + " " + pent.name())
        paramList.append(",")
      }
      if (paramList.length > 0)
      {
        paramList.setLength(paramList.length - 1)
      }

      System.out.print(paramList + "\n")
    }

    for(ent <- funcns)
    {
      val paramList = new StringBuilder()
      val paramrefs:Array[Reference] = ent.refs("define", "parameter", true)
      for (prf <- paramrefs)
      {
        val pent = prf.ent()
        // type ticks needed for scala (unlike java)
        paramList.append(pent.`type`() + " " + pent.name())
        paramList.append(",")
      }
      if (paramList.length > 0)
      {
        paramList.setLength(paramList.length - 1)
      }

      System.out.print(paramList + "\n")
    } */
}