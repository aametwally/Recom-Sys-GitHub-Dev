//import java.nio.file.{Files, Paths}
//import org.junit._
//import org.scalatest.FunSuite
//
///**
//  * Created by EricW on 11/9/2016.
//  * // unit and integration tests
//  */
//class ComparisonActorTest extends FunSuite {
//    var da_obj = new AnalyzerActor.ComparisonActor
//    // test creation of udb files
//    test("unit test 1") {
//      val x = da_obj.createUDBFiles("square")
//      assert(Files.exists(Paths.get("/okio.udb")))
//      assert(Files.exists(Paths.get("/Okio_v2.udb")))
//      println("Passed...")
//    }
//    // test creation of dependency graphs
//    test("unit test 2") {
//      da_obj.createDepGraphs(Array("okio", "okio_v2"))
//      assert(da_obj.dg_objs(0).vertexSet().size() == 1000)
//      assert(da_obj.dg_objs(1).vertexSet().size() == 1034)
//      assert(da_obj.dg_objs(0).edgeSet().size() == 100)
//      assert(da_obj.dg_objs(1).edgeSet().size() == 200)
//      println("Passed...")
//    }
//
//    // test analysis results
//    test("unit test 3") {
//      da_obj.analyzeResults()
//      assert(da_obj.num_added_nodes == 34)
//      assert(da_obj.num_deleted_nodes == 0)
//      println("Passed...")
//    }
//
//    test("integraton test") {
//      val x = da_obj.createUDBFiles("square")
//      da_obj.createDepGraphs(Array("okio", "okio_v2"))
//      da_obj.analyzeResults()
//      assert(da_obj.num_added_nodes == 34)
//      assert(da_obj.num_deleted_nodes == 0)
//      println("Passed...")
//    }
//}
